package com.theshowsoftware.UpDownProject.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RoundSettingRequestDTO {

    private int roundInterval; // 라운드 간격

    private int bettingTime; // 배팅 가능 시간

    private int bettingLockTime; // 배팅 잠금 시간

    private int processingTime; // 게임 진행 시간

    private int resultTime; // 결과 송출 시간
}