package com.theshowsoftware.UpDownProject.dto;

import com.theshowsoftware.UpDownProject.domain.RoundSettingEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundSettingResponseDTO {
    private int bettingTime;
    private int bettingLockTime;
    private int processingTime;
    private int resultTime;

    public static RoundSettingResponseDTO fromEntity(RoundSettingEntity entity) {
        return new RoundSettingResponseDTO(
                entity.getBettingTime(),
                entity.getBettingLockTime(),
                entity.getProcessingTime(),
                entity.getResultTime()
        );
    }
}