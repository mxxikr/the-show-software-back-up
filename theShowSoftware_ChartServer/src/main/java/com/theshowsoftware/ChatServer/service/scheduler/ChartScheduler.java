package com.theshowsoftware.ChatServer.service.scheduler;

import com.theshowsoftware.ChatServer.dto.CandlePacketDTO;
import com.theshowsoftware.ChatServer.dto.TickPacketDTO;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import com.theshowsoftware.ChatServer.service.ChartCacheService;
import com.theshowsoftware.ChatServer.service.PacketSenderService;
import com.theshowsoftware.ChatServer.utils.PacketManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChartScheduler {
    private final ChartCacheService chartCacheService = new ChartCacheService();
    private final PacketSenderService packetSenderService;

    private BigInteger currentPrice = BigInteger.valueOf(100_000_000_000L); // $50,000 * 10^9
    private final Random random = new Random();

    /**
     * Tick 데이터를 1초마다 생성하여 캐시에 추가
     */
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    private void generateTick() {
        BigInteger minValue = BigInteger.valueOf(100_000_000_000L);  // 100,000 * 10^9
        BigInteger maxValue = BigInteger.valueOf(199_999_990_000L);  // 199,999.99 * 10^9

        // 10% 범위에서 가격 변동
        double fluctuationRate = (random.nextDouble() * 10 - 5) / 100;

        // 현재 가격의 5% 변동
        BigInteger fluctuation = currentPrice.multiply(BigInteger.valueOf((long) (fluctuationRate * 1_000_000)))
                .divide(BigInteger.valueOf(100_000_000));

        currentPrice = currentPrice.add(fluctuation);

        // 가격 제한 적용
        if (currentPrice.compareTo(minValue) < 0) {
            currentPrice = minValue; // 최저 값 설정
        } else if (currentPrice.compareTo(maxValue) > 0) {
            currentPrice = maxValue; // 최대 값 설정
        }


        if (currentPrice.compareTo(minValue) < 0) {
            currentPrice = minValue; // 최저값 설정
            log.warn("[ChartScheduler] 가격이 최소값보다 낮아 초기화되었습니다.");
        } else if (currentPrice.compareTo(maxValue) > 0) {
            currentPrice = maxValue; // 최대값 설정
            log.warn("[ChartScheduler] 가격이 최대값보다 높아 초기화되었습니다.");
        }

        long now = Instant.now().toEpochMilli(); // 현재 시간
        long quantity = random.nextInt(100) + 1; // 거래량

        // 틱 데이터 생성
        TickPacketDTO tickPacketDTO = new TickPacketDTO(currentPrice, quantity, now);

        // 캐시에 틱 데이터 추가
        chartCacheService.addTick(SymbolType.BTC, tickPacketDTO);

        String packet = PacketManager.createTickPacket(SymbolType.BTC, tickPacketDTO);

        // 패킷 전송
        packetSenderService.sendTickDataToUser(SymbolType.BTC, SymbolType.BTC.name(), packet);

        log.info("[ChartSchedular] 틱 패킷 데이터 : " + packet);
    }

    /**
     * 모든 ChartType에 대해 1분마다 Candle 데이터를 생성하여 업데이트
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    private void generateCandles() {
        try {
            // 등록된 모든 ChartType에 대해 캔들을 생성
            for (ChartType chartType : ChartType.values()) {
                generateCandle(chartType); // 각 차트 타입에 대해 캔들 생성
            }
        } catch (Exception e) {
            log.error("Error generating Candles: ", e);
        }
    }

    /**
     * 특정 ChartType에 대한 Candle 데이터 생성 및 캐시 업데이트
     */
    private void generateCandle(ChartType chartType) {
        long intervalInMillis = chartType.getIntervalInMillis();

        List<TickPacketDTO> ticks = chartCacheService.getTicksBetween(
                SymbolType.BTC,
                Instant.now().minusMillis(intervalInMillis),
                Instant.now()
        );

        if (ticks.isEmpty()) return;

        ticks.sort(Comparator.comparingLong(TickPacketDTO::getTimestamp));
        long startTime = ticks.get(0).getTimestamp(); // 캔들 시작 시간
        long endTime = ticks.get(ticks.size() - 1).getTimestamp(); // 캔들 종료 시간
        BigInteger startPrice = ticks.get(0).getPrice(); // 시가
        BigInteger endPrice = ticks.get(ticks.size() - 1).getPrice(); // 종가
        BigInteger highPrice = ticks.stream().map(TickPacketDTO::getPrice).max(Comparator.naturalOrder()).orElse(startPrice); // 고가
        BigInteger lowPrice = ticks.stream().map(TickPacketDTO::getPrice).min(Comparator.naturalOrder()).orElse(startPrice); // 저가
        long quantity = ticks.stream().mapToLong(TickPacketDTO::getQuantity).sum(); // 총 거래량
        int tickCount = ticks.size(); // 틱 개수

        // Candle 데이터 생성
        CandlePacketDTO candlePacketDTO = CandlePacketDTO.builder()
                .symbolType(SymbolType.BTC)
                .chartType(chartType)
                .candleStartTime(startTime)
                .candleEndTime(endTime)
                .startPrice(startPrice)
                .endPrice(endPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .quantity(quantity)
                .tickCount(tickCount)
                .build();

        chartCacheService.addCandle(SymbolType.BTC, chartType, candlePacketDTO);

        // 패킷 생성
        String packet = PacketManager.createCandlePacket(candlePacketDTO);

        // 패킷 전송
        packetSenderService.sendCandleDataToUser(SymbolType.BTC, SymbolType.BTC.name(), packet);

        log.info("[ChartScheduler] Candle Packet ({}): {}", chartType.getLabel(), packet);
    }
}
