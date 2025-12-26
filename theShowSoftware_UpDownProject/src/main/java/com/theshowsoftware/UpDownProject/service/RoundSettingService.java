package com.theshowsoftware.UpDownProject.service;

import com.theshowsoftware.UpDownProject.dto.RoundSettingRequestDTO;
import com.theshowsoftware.UpDownProject.dto.RoundSettingResponseDTO;

public interface RoundSettingService {
    RoundSettingResponseDTO getCurrentSettings();

    RoundSettingResponseDTO updateRoundSettings(RoundSettingRequestDTO request);
}