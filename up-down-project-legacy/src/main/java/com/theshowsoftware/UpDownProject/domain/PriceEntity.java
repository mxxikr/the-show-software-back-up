package com.theshowsoftware.UpDownProject.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol; // BTCUSDT

    private BigDecimal price; // 가격

    private String exchange; // 거래소

    @Column(name = "timestamp", columnDefinition = "TIMESTAMP(3)")
    private LocalDateTime timestamp; // 타임스탬프
}