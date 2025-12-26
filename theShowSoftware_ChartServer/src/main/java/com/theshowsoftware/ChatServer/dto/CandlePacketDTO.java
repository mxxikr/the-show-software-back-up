package com.theshowsoftware.ChatServer.dto;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandlePacketDTO {
    private SymbolType symbolType;      // 심볼
    private ChartType chartType;        // 차트 유형
    private BigInteger startPrice;      // 시가 (10^9 곱해진 값)
    private BigInteger  endPrice;      // 종가 (10^9 곱해진 값)
    private BigInteger highPrice;       // 고가 (10^9 곱해진 값)
    private BigInteger lowPrice;     // 저가 (10^9 곱해진 값)
    private Long quantity;    // 거래량
    private int tickCount;    // 변동 틱 개수
    private Long candleStartTime; // 캔들 시작 시간 (epoch millis)
    private Long candleEndTime;   // 캔들 종료 시간 (epoch millis)
}