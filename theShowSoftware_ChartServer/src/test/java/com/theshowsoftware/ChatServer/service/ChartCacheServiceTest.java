package com.theshowsoftware.ChatServer.service;

import com.theshowsoftware.ChatServer.dto.CandlePacketDTO;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import com.theshowsoftware.ChatServer.exception.CustomException;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChartCacheServiceTest {

    private final ChartCacheService chartCache = new ChartCacheService();

    @Test
    public void addCandleValidData() {
        SymbolType symbol = SymbolType.BTC;
        ChartType chartType = ChartType.ONE_MINUTE;
        CandlePacketDTO candle = CandlePacketDTO.builder()
                .candleStartTime(Instant.now().minusSeconds(60).toEpochMilli())
                .candleEndTime(Instant.now().toEpochMilli())
                .startPrice(BigInteger.valueOf(1000))
                .endPrice(BigInteger.valueOf(1050))
                .highPrice(BigInteger.valueOf(1100))
                .lowPrice(BigInteger.valueOf(990))
                .quantity(1000L)
                .tickCount(10)
                .build();

        // 실행
        chartCache.addCandle(symbol, chartType, candle);

        // 검증
        List<CandlePacketDTO> candles = chartCache.getCandles(symbol, chartType, 10);
        assertEquals(1, candles.size(), "Candle 데이터가 저장되지 않았습니다.");
        CandlePacketDTO storedCandle = candles.get(0);
        assertEquals(candle.getStartPrice(), storedCandle.getStartPrice(), "시작 가격이 올바르지 않습니다.");
        assertEquals(candle.getEndPrice(), storedCandle.getEndPrice(), "종료 가격이 올바르지 않습니다.");
        assertEquals(candle.getHighPrice(), storedCandle.getHighPrice(), "최고 가격이 올바르지 않습니다.");
        assertEquals(candle.getLowPrice(), storedCandle.getLowPrice(), "최저 가격이 올바르지 않습니다.");
    }

    @Test
    public void addCandleNullInputs() {
        SymbolType symbol = SymbolType.BTC;
        ChartType chartType = ChartType.ONE_MINUTE;

        // Null symbol 입력 확인
        CustomException exception = assertThrows(CustomException.class, () ->
                chartCache.addCandle(null, chartType, new CandlePacketDTO())
        );
        assertEquals("CACHE_DATA_IS_NULL", exception.getErrorCode().name());

        // Null chartType 입력 확인
        exception = assertThrows(CustomException.class, () ->
                chartCache.addCandle(symbol, null, new CandlePacketDTO())
        );
        assertEquals("CACHE_DATA_IS_NULL", exception.getErrorCode().name());

        // Null candle 입력 확인
        exception = assertThrows(CustomException.class, () ->
                chartCache.addCandle(symbol, chartType, null)
        );
        assertEquals("CACHE_DATA_IS_NULL", exception.getErrorCode().name());
    }

    @Test
    public void addCandleCacheOverflow() {
        // Given
        SymbolType symbol = SymbolType.BTC;
        ChartType chartType = ChartType.ONE_MINUTE;
        int candleLimit = 10080;
        Instant currentInstant = Instant.now();

        for (int i = 0; i < candleLimit + 10; i++) {
            Instant startTime = currentInstant.minusSeconds(60L * (candleLimit + 10 - i));

            CandlePacketDTO candle = CandlePacketDTO.builder()
                    .symbolType(symbol)
                    .chartType(chartType)
                    .candleStartTime(startTime.toEpochMilli())
                    .candleEndTime(startTime.plusSeconds(60).toEpochMilli())
                    .startPrice(BigInteger.valueOf(1000 + i))
                    .endPrice(BigInteger.valueOf(1050 + i))
                    .highPrice(BigInteger.valueOf(1100 + i))
                    .lowPrice(BigInteger.valueOf(990 + i))
                    .quantity(1000L + i)
                    .tickCount(10 + i)
                    .build();
            chartCache.addCandle(symbol, chartType, candle);
        }

        // When
        List<CandlePacketDTO> candles = chartCache.getCandles(symbol, chartType, candleLimit + 10);
        assertEquals(candleLimit, candles.size(), "캔들 데이터가 캐시 크기 제한을 초과했습니다.");

        CandlePacketDTO firstCandle = candles.get(0); // 가장 오래된 데이터
        CandlePacketDTO lastCandle = candles.get(candles.size() - 1); // 최신 데이터

        System.out.println("첫 번째 데이터: " + firstCandle);
        System.out.println("마지막 데이터: " + lastCandle);

        assertTrue(firstCandle.getCandleStartTime() < lastCandle.getCandleStartTime(), "오래된 데이터가 먼저 있어야 합니다.");
    }

    @Test
    public void getCandlesEmptyCache() {
        SymbolType symbol = SymbolType.BTC;
        ChartType chartType = ChartType.ONE_MINUTE;

        List<CandlePacketDTO> candles = chartCache.getCandles(symbol, chartType, 10);
        assertTrue(candles.isEmpty(), "빈 캐시에서 데이터가 반환되지 않았습니다.");
    }
}