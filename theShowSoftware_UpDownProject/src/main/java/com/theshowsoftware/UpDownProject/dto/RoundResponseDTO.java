package com.theshowsoftware.UpDownProject.dto;

import com.theshowsoftware.UpDownProject.domain.RoundEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.theshowsoftware.UpDownProject.enums.ResultType;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundResponseDTO {
    private long id;
    private Integer roundNum;
    private String gameStartTime;
    private String gameEndTime;
    private String roundStartTime;
    private String roundEndTime;
    private BigDecimal gameStartPrice;
    private BigDecimal gameEndPrice;
    private RoundStatus roundStatus;
    private LocalDate roundDate;
    private ResultType roundResult;

    public static RoundResponseDTO fromEntity(RoundEntity roundEntity) {
        return RoundResponseDTO.builder()
                .id(roundEntity.getId())
                .roundNum(roundEntity.getRoundNum())
                .roundStartTime(roundEntity.getRoundStartTime())
                .roundEndTime(roundEntity.getRoundEndTime())
                .gameStartTime(roundEntity.getGameStartTime())
                .gameEndTime(roundEntity.getGameEndTime())
                .gameStartPrice(roundEntity.getGameStartPrice())
                .gameEndPrice(roundEntity.getGameEndPrice())
                .roundStatus(roundEntity.getRoundStatus())
                .roundResult(roundEntity.getRoundResult())
                .roundDate(roundEntity.getRoundDate())
                .build();
    }
}