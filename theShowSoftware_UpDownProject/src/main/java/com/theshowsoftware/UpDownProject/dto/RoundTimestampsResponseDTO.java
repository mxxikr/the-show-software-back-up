package com.theshowsoftware.UpDownProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RoundTimestampsResponseDTO {
    private Long roundId;             // 진행 중인 라운드 ID
    private Long serverTimestamp;     // 서버 현재 시간
    private Long gameStartTimestamp;  // 게임 진행 시작 시간
    private Long gameEndTimestamp;    // 게임 종료 시간
}
