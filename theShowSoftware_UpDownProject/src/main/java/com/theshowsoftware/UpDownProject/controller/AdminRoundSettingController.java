package com.theshowsoftware.UpDownProject.controller;

import com.theshowsoftware.UpDownProject.dto.RoundSettingRequestDTO;
import com.theshowsoftware.UpDownProject.dto.RoundSettingResponseDTO;
import com.theshowsoftware.UpDownProject.scheduler.RoundScheduler;
import com.theshowsoftware.UpDownProject.service.RoundSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRoundSettingController {
    private final RoundSettingService settingService;
    private final RoundScheduler roundScheduler;

    /**
     * 현재 라운드 설정 조회
     */
    @GetMapping("/get_round_settings")
    public ResponseEntity<RoundSettingResponseDTO> getRoundSettings() {
        RoundSettingResponseDTO responseDTO = settingService.getCurrentSettings();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 라운드 설정 업데이트
     * - 업데이트 후 스케줄 재등록
     */
    @PostMapping("/update_round_settings")
    public ResponseEntity<RoundSettingResponseDTO> updateRoundSettings(@RequestBody RoundSettingRequestDTO roundRequestDTO) {
        RoundSettingResponseDTO updatedSetting = settingService.updateRoundSettings(roundRequestDTO);

        // 새로운 설정을 반영
        roundScheduler.onRoundSettingsUpdated();
        return ResponseEntity.ok(updatedSetting);
    }
}
