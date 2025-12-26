package com.theshowsoftware.ChatServer.utils;

import com.theshowsoftware.ChatServer.dto.CandlePacketDTO;
import com.theshowsoftware.ChatServer.dto.TickPacketDTO;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.ErrorCode;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import com.theshowsoftware.ChatServer.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

@Slf4j
public class PacketManager {

    private static final String PACKET_START = "!";
    private static final String PACKET_END = "#";
    private static final String DELIMITER = ";";

    /**
     * 캔들 데이터 패킷 생성
     * default format - !코인코드;기준통화;패킷유형;차트타입;캔들데이터#
     */
    public static String createCandlePacket(CandlePacketDTO candlePacketDTO) {
        return PACKET_START +
                SymbolType.USDT.getHexCodeString() + DELIMITER +
                candlePacketDTO.getSymbolType().getHexCodeString() + DELIMITER +
                candlePacketDTO.getChartType().getHexCode() + DELIMITER +
                candlePacketDTO.getCandleStartTime() + DELIMITER +
                candlePacketDTO.getCandleEndTime() + DELIMITER +
                candlePacketDTO.getStartPrice() + DELIMITER +
                candlePacketDTO.getEndPrice() + DELIMITER +
                candlePacketDTO.getHighPrice() + DELIMITER +
                candlePacketDTO.getLowPrice() + DELIMITER +
                candlePacketDTO.getQuantity() + DELIMITER +
                candlePacketDTO.getTickCount() +
                PACKET_END;
    }

    /**
     * 틱 데이터 패킷 생성
     * default format - !코인코드;기준통화;패킷유형;차트타입;가격데이터#
     */
    public static String createTickPacket(SymbolType symbolType, TickPacketDTO tickPacketDTO) {
        return PACKET_START +
                SymbolType.USDT.getHexCodeString() + DELIMITER +
                symbolType.getHexCodeString() + DELIMITER +
                ChartType.TICK.getHexCode() + DELIMITER +
                tickPacketDTO.getPrice() + DELIMITER +
                tickPacketDTO.getQuantity() + DELIMITER +
                tickPacketDTO.getTimestamp() + PACKET_END;
    }

    /**
     * 캔들 패킷 디패키징
     */
    public static CandlePacketDTO parseCandlePacket(String packet) {
        try {
            if (!isPacketValid(packet)) {
                throw new CustomException(ErrorCode.INVAILD_PACKET_STRUCTURE);
            }

            String[] parts = extractPacketData(packet);
            log.info("파싱된 패킷 데이터 : " + String.join(", ", parts));

            if (parts.length != 11) {
                throw new CustomException(ErrorCode.PACKET_INVALID_PARTS_COUNT);
            }

            SymbolType baseSymbol = SymbolType.fromHexCode(parts[0]);
            if (baseSymbol != SymbolType.USDT) {
                throw new CustomException(ErrorCode.PACKET_BASE_SYMBOL_INVALID);
            }

            return CandlePacketDTO.builder()
                    .symbolType(SymbolType.fromHexCode(parts[1]))
                    .chartType(getChartTypeFromCode(parts[2]))
                    .candleStartTime(Long.parseLong(parts[3]))
                    .candleEndTime(Long.parseLong(parts[4]))
                    .startPrice(new BigInteger(parts[5]))
                    .endPrice(new BigInteger(parts[6]))
                    .highPrice(new BigInteger(parts[7]))
                    .lowPrice(new BigInteger(parts[8]))
                    .quantity(Long.parseLong(parts[9]))
                    .tickCount(Integer.parseInt(parts[10]))
                    .build();
        }  catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILD_CANDLE_PARSE_ERROR);
        }
    }

    /**
     * 틱 패킷 디패키징
     */
    public static TickPacketDTO parseTickPacket(String packet) {
        try {
            if (!isPacketValid(packet)) {
                throw new IllegalArgumentException("유효하지 않은 패킷 포맷입니다." + packet);
            }

            String[] parts = extractPacketData(packet);

            if (parts.length != 6) {
                throw new IllegalArgumentException("유효하지 않은 패킷 구조체입니다." + packet);
            }

            return TickPacketDTO.builder()
                    .price(new BigInteger(parts[3]))
                    .quantity(Long.parseLong(parts[4]))
                    .timestamp(Long.parseLong(parts[5]))
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_TICK_PARSE_ERROR);
        }
    }

    /**
     * 패킷 유효성 검사
     */
    private static boolean isPacketValid(String packet) {
        return packet.startsWith(PACKET_START) && packet.endsWith(PACKET_END);
    }

    /**
     * 패킷에서 실제 데이터를 추출
     */
    private static String[] extractPacketData(String packet) {
        // 패킷 시작과 끝 문자 제거 후 데이터 분리
        return packet.substring(1, packet.length() - 1).split(DELIMITER);
    }

    /**
     * 차트 HexCode로 ChartType 변환
     */
    private static ChartType getChartTypeFromCode(String hexCode) {
        try {
            for (ChartType type : ChartType.values()) {
                if (type.getHexCode().equalsIgnoreCase(hexCode)) {
                    return type;
                }
            }
            throw new CustomException(ErrorCode.UNKOWUN_CHART_TYPE);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKOWUN_CHART_TYPE);
        }
    }
}