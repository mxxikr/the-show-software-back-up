package com.theshowsoftware.UpDownProject.dto;

import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoundRequestDTO {
    private LocalDate roundDate;
    private long id;
    private RoundStatus roundStatus;
    private BigDecimal gameStartPrice;
    private BigDecimal gameEndPrice;
    private Integer roundNum;
}