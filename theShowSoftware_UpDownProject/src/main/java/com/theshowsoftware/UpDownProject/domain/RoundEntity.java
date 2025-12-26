package com.theshowsoftware.UpDownProject.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.theshowsoftware.UpDownProject.enums.ResultType;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "round",
        indexes = @Index(name = "idx_round_date_num", columnList = "round_date, round_num")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 라운드 고유 번호

    @Column(nullable = false)
    private Integer roundNum; // 해당 날짜의 라운드 번호

    @Column(nullable = false)
    private LocalDate roundDate; // 라운드 날짜 (연월일)

    private String roundStartTime; // 해당 라운드가 시작한 시간

    private String roundEndTime; // 해당 라운드가 종료된 시간

    private String gameStartTime; // 해당 라운드의 게임 진행 단계 시작 시간

    private String gameEndTime; // 해당 라운드의 게임 진행 단계 종료 시간

    private BigDecimal gameStartPrice;  // 게임 진행 단계 시작 시점의 비트 코인 가격

    private BigDecimal gameEndPrice; // 게임 진행 단계 종료 시점의 비트 코인 가격

    @Enumerated(EnumType.STRING)
    private ResultType roundResult; // 라운드 결과

    @Enumerated(EnumType.STRING)
    private RoundStatus roundStatus; // 진행 상태
}