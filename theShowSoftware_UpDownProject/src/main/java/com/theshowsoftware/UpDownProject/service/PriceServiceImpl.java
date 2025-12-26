package com.theshowsoftware.UpDownProject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theshowsoftware.UpDownProject.domain.PriceEntity;
import com.theshowsoftware.UpDownProject.dto.PriceResponseDTO;
import com.theshowsoftware.UpDownProject.enums.Exchange;
import com.theshowsoftware.UpDownProject.exception.CustomException;
import com .theshowsoftware.UpDownProject.enums.ErrorCode;
import com.theshowsoftware.UpDownProject.repository.PriceRepository;
import com.theshowsoftware.UpDownProject.util.TimeFormatUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceServiceImpl implements PriceService {
    @Value("${binance.api.url}")
    private String binanceApiUrl;
    @Value("${okx.api.url}")
    private String okxApiUrl;
    @Value("${huobi.api.url}")
    private String huobiApiUrl;
    @Value("${kucoin.api.url}")
    private String kucoinApiUrl;
    @Value("${mexc.api.url}")
    private String mexcApiUrl;
    @Value("${bybit.api.url}")
    private String bybitApiUrl;

    @Value("${binance.api.symbol}")
    private String binanceSymbol;
    @Value("${okx.api.symbol}")
    private String okxSymbol;
    @Value("${huobi.api.symbol}")
    private String huobiSymbol;
//    @Value("${kucoin.api.symbol}")
//    private String kucoinSymbol;
//    @Value("${mexc.api.symbol}")
//    private String mexcSymbol;
//    @Value("${bybit.api.symbol}")
//    private String bybitSymbol;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PriceRepository priceRepository;

    private static final int PRICE_SAVE_LIMIT = 200; // 최근 100초 데이터 저장
    private static final int BULK_SAVE_THRESHOLD = 50; // 50개 이상 모이면 벌크 저장

    private final Queue<PriceResponseDTO> priceDeque = new ArrayBlockingQueue<>(PRICE_SAVE_LIMIT);  // 최근 100초 가격 정보를 담는 큐
    private final List<PriceEntity> priceBuffer = Collections.synchronizedList(new ArrayList<>()); // DB 저장을 위한 버퍼

    /**
     * Binance, OKX, Huobi에서 데이터를 가져와 DB에 저장하고 평균 값 계산
     */
    @Override
    public BigDecimal fetchCurrentPrice(boolean isRoundSchedulerCall) {
        LocalDateTime timestamp = LocalDateTime.now();

        CompletableFuture<PriceResponseDTO> binanceFuture = CompletableFuture.supplyAsync(() ->
                fetchPriceFromApi(binanceApiUrl, binanceSymbol, Exchange.BINANCE, timestamp)
        );

        CompletableFuture<PriceResponseDTO> okxFuture = CompletableFuture.supplyAsync(() ->
                fetchPriceFromApi(okxApiUrl, okxSymbol, Exchange.OKX, timestamp)
        );

        CompletableFuture<PriceResponseDTO> huobiFuture = CompletableFuture.supplyAsync(() ->
                fetchPriceFromApi(huobiApiUrl, huobiSymbol, Exchange.HUOBI, timestamp)
        );

//        CompletableFuture<PriceResponseDTO> kucoinFuture = CompletableFuture.supplyAsync(() ->
//                fetchPriceFromApi(kucoinApiUrl, kucoinSymbol, Exchange.KUCOIN, timestamp)
//        );
//
//        CompletableFuture<PriceResponseDTO> mexcFuture = CompletableFuture.supplyAsync(() ->
//                fetchPriceFromApi(mexcApiUrl, mexcSymbol, Exchange.MEXC, timestamp)
//        );
//
//        CompletableFuture<PriceResponseDTO> bybitFuture = CompletableFuture.supplyAsync(() ->
//                fetchPriceFromApi(bybitApiUrl, bybitSymbol, Exchange.BYBIT, timestamp)
//        );

        List<PriceResponseDTO> responses = CompletableFuture.allOf(
                binanceFuture, okxFuture, huobiFuture
//                kucoinFuture, mexcFuture, bybitFuture
        ).thenApply(v -> Arrays.asList(
                binanceFuture.join(),
                okxFuture.join(),
                huobiFuture.join()
//                kucoinFuture.join(),
//                mexcFuture.join(),
//                bybitFuture.join()
        )).join();


        // 원본 데이터를 DB에 저장
        if (!isRoundSchedulerCall) {
            // 스케줄러 호출이 아닌 경우 원본 데이터를 DB에 저장
            saveOriginalPrices(responses);
        }

        // 평균값 계산
        return calculateResultPrice(responses);
    }

    /**
     * 실시간 데이터 반환
     */
    @Override
    public String getCurrentPrices(BigDecimal latestPrice) {
        long timestamp = TimeFormatUtil.getCurrentTimestamp();

        try {
            Map<String, Object> priceData = Map.of(
                    "price", latestPrice.toPlainString(),
                    "timestamp", timestamp
            );
            return new ObjectMapper().writeValueAsString(priceData);
        } catch (Exception e) {
            log.error("[PriceService] 실시간 가격 JSON 생성 실패 - error={}", e.getMessage());
            throw new CustomException(ErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }

    /**
     * 최근 100초 동안의 가격 반환
     */
    @Override
    public Map<String, Object> getHistoryPrices() {
        long serverTimestamp = TimeFormatUtil.getCurrentTimestamp();
        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> prices = new ArrayList<>();

        synchronized (priceDeque) {
            for (PriceResponseDTO price : priceDeque) {
                if (price.getTimestamp().isAfter(now.minusSeconds(PRICE_SAVE_LIMIT))) {
                    prices.add(Map.of(
                            "price", price.getPrice().toPlainString(),
                            "timestamp", price.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli()
                    ));
                }
            }
        }

        return Map.of(
                "serverTimestamp", serverTimestamp,
                "prices", prices
        );
    }

    /**
     * API 요청 및 데이터 가져오기
     */
    private PriceResponseDTO fetchPriceFromApi(String apiUrl, String apiSymbol, Exchange exchange, LocalDateTime timestamp) {
        try {
            String requestUrl = apiUrl + apiSymbol;
            String response = restTemplate.getForObject(requestUrl, String.class);

            JsonNode rootNode = objectMapper.readTree(response); // JSON 파싱
            String priceString = null;

            // 거래소에 따른 데이터 파싱
            switch (exchange) {
                case BINANCE:
                    JsonNode priceNode = rootNode.get("price");
                    if (priceNode != null) {
                        priceString = priceNode.asText();
                    } else {
                        log.error("[PriceService] {} API 파싱 오류 : {}", exchange, rootNode);
                        throw new CustomException(ErrorCode.API_PARSE_ERROR);
                    }
                    break;

                case OKX:
                    JsonNode dataNode = rootNode.get("data");
                    if (dataNode != null && dataNode.isArray() && dataNode.size() > 0) {
                        JsonNode firstData = dataNode.get(0);
                        if (firstData != null && firstData.has("last")) {
                            priceString = firstData.get("last").asText();
                        } else {
                            log.error("[PriceService] {} API last 값 파싱 오류 : {}", exchange, rootNode);
                            throw new CustomException(ErrorCode.API_PARSE_ERROR);
                        }
                    } else {
                        log.error("[PriceService] {} API data 값 파싱 오류 : {}", exchange, rootNode);
                        throw new CustomException(ErrorCode.API_PARSE_ERROR);
                    }
                    break;

                case HUOBI:
                    JsonNode tickNode = rootNode.get("tick");
                    if (tickNode == null || !tickNode.has("data")) {
                        log.error("[PriceService] {} API data.tick 값 파싱 오류 : {}", exchange, rootNode);
                        throw new CustomException(ErrorCode.API_PARSE_ERROR);
                    }

                    JsonNode huobiDataNode = tickNode.get("data");
                    if (huobiDataNode.isArray() && huobiDataNode.size() > 0) {
                        JsonNode firstData = huobiDataNode.get(0);
                        if (firstData != null && firstData.has("price")) {
                            priceString = firstData.get("price").asText();
                        } else {
                            log.error("[PriceService] {} API price 값 파싱 오류 : {}", exchange, rootNode);
                            throw new CustomException(ErrorCode.API_PARSE_ERROR);
                        }
                    } else {
                        log.error("[PriceService] {} API data 값 파싱 오류 : {}", exchange, rootNode);
                        throw new CustomException(ErrorCode.API_PARSE_ERROR);
                    }
                    break;
                case BYBIT:
                    JsonNode bybitResultNode = rootNode.get("result");
                    if (bybitResultNode != null && bybitResultNode.has("list")) {
                        JsonNode dataList = bybitResultNode.get("list");
                        if (dataList.isArray() && dataList.size() > 0) {
                            JsonNode firstData = dataList.get(0);
                            if (firstData != null && firstData.has("lastPrice")) {
                                priceString = firstData.get("lastPrice").asText();
                            }
                        }
                    }
                    break;

                case MEXC:
                    JsonNode mexcPriceNode = rootNode.get("price");
                    if (mexcPriceNode != null) {
                        priceString = mexcPriceNode.asText();
                    }
                    break;

                case KUCOIN:
                    JsonNode kucoinDataNode = rootNode.get("data");
                    if (kucoinDataNode != null && kucoinDataNode.has("price")) {
                        priceString = kucoinDataNode.get("price").asText();
                    }
                    break;

                default:
                    log.error("[PriceService] 알 수 없는 거래소: {}", exchange);
                    throw new CustomException(ErrorCode.API_ERROR);
            }

            if (priceString == null) {
                log.error("[PriceService] {} API 가격 데이터를 가져올 수 없습니다. 응답: {}", exchange, response);
                throw new CustomException(ErrorCode.API_NO_RESPONSE);
            }

            BigDecimal price = new BigDecimal(priceString);

            log.debug("[PriceService] {} API 현재 가격: {}", exchange, price);

            return PriceResponseDTO.builder()
                    .price(price)
                    .timestamp(timestamp) // 현재 시간
                    .exchange(exchange.toString()) // 거래소 이름
                    .symbol(apiSymbol.toUpperCase().replace("-", ""))
                    .build();

        } catch (Exception e) {
            log.error("[PriceService] {} API 데이터 가져오기 실패 - URL={}, error={}", exchange, apiUrl, e.getMessage());
            throw new CustomException(ErrorCode.API_ERROR);
        }
    }

    /**
     * 결과 값 계산
     * 평균 가격 + 각 거래소의 가격 차이 절대 값을 랜덤으로 선택한 후 해당 값의 4분의 1 범위를 랜덤으로 선택한 값
     */
    private BigDecimal calculateResultPrice(List<PriceResponseDTO> responses) {
        BigDecimal average = responses.stream()
                .map(PriceResponseDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(responses.size()), 8, BigDecimal.ROUND_HALF_UP);

        // 거래소별 가격 차이 계산 (|평균 - 최신 가격|)
        Map<Exchange, BigDecimal> priceDiffs = new HashMap<>();
        for (PriceResponseDTO response : responses) {
            BigDecimal priceDiff = average.subtract(response.getPrice()).abs(); // 절대값
            priceDiffs.put(Exchange.valueOf(response.getExchange()), priceDiff);
        }

        // 랜덤으로 하나의 절대 값 선택
        Random random = new Random();
        List<BigDecimal> priceDiffList = new ArrayList<>(priceDiffs.values());
        BigDecimal randomPriceDiff = priceDiffList.get(random.nextInt(priceDiffList.size())); // 랜덤 선택

        // 선택 된 절대 값의 절반 범위를 랜덤으로 선택
        BigDecimal halfRange = randomPriceDiff.divide(new BigDecimal("4"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal randomValue = halfRange
                .multiply(BigDecimal.valueOf(random.nextDouble() * 2 - 1))
                .setScale(8, BigDecimal.ROUND_HALF_UP);

        // 계산된 값을 평균에 더함
        BigDecimal finalResult = average.add(randomValue);

        LocalDateTime timestamp = LocalDateTime.now();

        // 큐에 최종 값 저장
        synchronized (priceDeque) {
            if (priceDeque.size() >= PRICE_SAVE_LIMIT) {
                priceDeque.poll(); // 오래된 데이터 제거
            }
            priceDeque.add(PriceResponseDTO.builder()
                    .price(finalResult)
                    .timestamp(timestamp)
                    .build());
        }

        log.debug("[PriceService] 최종 값 계산 완료 최종 가격={}, 저장 시간={}", finalResult, timestamp);
        return finalResult;
    }

    /**
     * 원본 데이터를 DB에 저장
     */
    private void saveOriginalPrices(List<PriceResponseDTO> responses) {
        List<PriceEntity> entities = new ArrayList<>();
        for (PriceResponseDTO dto : responses) {
            entities.add(PriceEntity.builder()
                    .symbol(dto.getSymbol())
                    .price(dto.getPrice())
                    .exchange(dto.getExchange())
                    .timestamp(dto.getTimestamp())
                    .build());
        }

        // 버퍼에 데이터 추가
        synchronized (priceBuffer) {
            priceBuffer.addAll(entities);

            // 벌크 저장 조건 확인 및 실행
            savePriceHistoryBulk();
        }

        log.debug("[PriceService] 원본 데이터 {}건 저장 완료", entities.size());
    }

    /**
    * 가격 데이터 벌크 저장
    */
    void savePriceHistoryBulk() {
        if (priceBuffer.size() >= BULK_SAVE_THRESHOLD) {
            List<PriceEntity> saveList = new ArrayList<>(priceBuffer);
            priceRepository.saveAll(saveList);
            priceBuffer.clear();
            log.debug("[PriceService] 벌크 저장 완료: {} 개", saveList.size());
        }
    }

    /**
     * 하루 지난 가격 데이터 삭제
     */
    @Scheduled(cron = "0 5 0 * * ?") // 매일 자정 5분 후 실행
    @Transactional
    public void deleteOldPriceHistory() {
        LocalDate todayMidnight = LocalDate.now();
        LocalDateTime cutoffDate = todayMidnight.atStartOfDay();

        int deletedCount = priceRepository.deleteByTimestampBefore(cutoffDate);
        log.info("[PriceService] 가격 데이터 삭제 완료: {} rows", deletedCount);
    }
}