package com.theshowsoftware.UpDownProject.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "round_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoundSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int bettingTime;   // 배팅 가능 시간

    private int bettingLockTime; // 배팅 잠금 시간

    private int processingTime;      // 게임 진행 시간

    private int resultTime;     // 결과 출력 시간
}