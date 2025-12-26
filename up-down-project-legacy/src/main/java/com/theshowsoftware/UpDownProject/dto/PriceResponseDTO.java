package com.theshowsoftware.UpDownProject.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceResponseDTO {
    private BigDecimal price;
    private LocalDateTime timestamp;
    private String exchange;
    private String symbol;
}