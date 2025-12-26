package com.theshowsoftware.ChatServer.utils;

import com.theshowsoftware.ChatServer.dto.CandlePacketDTO;
import com.theshowsoftware.ChatServer.dto.TickPacketDTO;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PacketManagerTest {

    /**
     * 캔들 패킷 생성 테스트
     */
    @Test
    public void createCandlePacket() {
        // Given
        CandlePacketDTO candle = CandlePacketDTO.builder()
                .symbolType(SymbolType.USDT)
                .symbolType(SymbolType.BTC)
                .chartType(ChartType.ONE_MINUTE)
                .candleStartTime(1633017600000L)
                .candleEndTime(1633017660000L)
                .startPrice(BigInteger.valueOf(50000000000L))
                .endPrice(BigInteger.valueOf(51000000000L))
                .highPrice(BigInteger.valueOf(52000000000L))
                .lowPrice(BigInteger.valueOf(49000000000L))
                .quantity(100000L)
                .tickCount(0)
                .build();

        // When
        String packet = PacketManager.createCandlePacket(candle);

        // Then
        String expected = "!0x0000;0x000A;0x06;1633017600000;1633017660000;50000000000;51000000000;52000000000;49000000000;100000;0#";

        assertEquals(expected, packet, "캔들 패킷 생성 결과가 예상과 다릅니다.");
    }

    /**
     * 틱 패킷 생성 테스트
     */
    @Test
    public void createTickPacket() {
        // Given
        TickPacketDTO tick = TickPacketDTO.builder()
                .price(BigInteger.valueOf(52000L))
                .quantity(200L)
                .timestamp(1633017800000L)
                .build();

        // When
        String packet = PacketManager.createTickPacket(SymbolType.ETH, tick);

        // Then
        String expected = "!0x0000;0x0015;0x00;52000;200;1633017800000#";
        assertEquals(expected, packet, "틱 패킷이 예상과 일치하지 않습니다.");
    }

    /**
     * 틱 패킷 생성 및 파싱 테스트
     */
    @Test
    public void createAndParseTickPacket() {
        // Given
        TickPacketDTO tick = TickPacketDTO.builder()
                .price(BigInteger.valueOf(52000000000L))
                .quantity(1500L)
                .timestamp(1633018000000L)
                .build();
        SymbolType symbolType = SymbolType.DOT;

        // When
        String packet = PacketManager.createTickPacket(symbolType, tick);
        TickPacketDTO parsedTick = PacketManager.parseTickPacket(packet);

        // Then
        assertEquals(tick.getPrice(), parsedTick.getPrice());
        assertEquals(tick.getQuantity(), parsedTick.getQuantity());
        assertEquals(tick.getTimestamp(), parsedTick.getTimestamp());
    }

    /**
     * 캔들 데이터 패킷 생성 및 파싱 테스트
     */
    @Test
    public void createAndParseCandlePacket() {
        // Given
        CandlePacketDTO candle = CandlePacketDTO.builder()
                .symbolType(SymbolType.SOL)
                .chartType(ChartType.FIVE_MINUTES)
                .candleStartTime(1633018400000L)
                .candleEndTime(1633018700000L)
                .startPrice(BigInteger.valueOf(55000000000L))
                .endPrice(BigInteger.valueOf(56000000000L))
                .highPrice(BigInteger.valueOf(57000000000L))
                .lowPrice(BigInteger.valueOf(54000000000L))
                .quantity(120000L)
                .tickCount(10)
                .build();

        // When
        String packet = PacketManager.createCandlePacket(candle);
        System.out.println("생성 된 패킷 :" + packet);

        CandlePacketDTO parsedCandle = PacketManager.parseCandlePacket(packet);

        // Then
        assertEquals(candle.getSymbolType(), parsedCandle.getSymbolType());
        assertEquals(candle.getChartType(), parsedCandle.getChartType());
        assertEquals(candle.getCandleStartTime(), parsedCandle.getCandleStartTime());
        assertEquals(candle.getCandleEndTime(), parsedCandle.getCandleEndTime());
        assertEquals(candle.getStartPrice(), parsedCandle.getStartPrice());
        assertEquals(candle.getEndPrice(), parsedCandle.getEndPrice());
        assertEquals(candle.getHighPrice(), parsedCandle.getHighPrice());
        assertEquals(candle.getLowPrice(), parsedCandle.getLowPrice());
        assertEquals(candle.getQuantity(), parsedCandle.getQuantity());
        assertEquals(candle.getTickCount(), parsedCandle.getTickCount());
    }

    /**
     * 잘못된 패킷 처리 테스트
     */
    @Test
    public void invalidPacket() {
        // Given
        String invalidPacket = "!0x0015;0x0000;INVALID;123456789#";

        // When & Then
        assertThrows(RuntimeException.class, () -> PacketManager.parseTickPacket(invalidPacket));
    }
}