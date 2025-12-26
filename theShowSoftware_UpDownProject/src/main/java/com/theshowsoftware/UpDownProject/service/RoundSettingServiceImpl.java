package com.theshowsoftware.UpDownProject.service;

import com.theshowsoftware.UpDownProject.domain.RoundSettingEntity;
import com.theshowsoftware.UpDownProject.dto.RoundSettingRequestDTO;
import com.theshowsoftware.UpDownProject.dto.RoundSettingResponseDTO;
import com.theshowsoftware.UpDownProject.enums.ErrorCode;
import com.theshowsoftware.UpDownProject.exception.CustomException;
import com.theshowsoftware.UpDownProject.repository.RoundSettingRepository;
import com.theshowsoftware.UpDownProject.service.event.RoundSettingsUpdatedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 라운드 세팅(시간 관련) 관리
 * - BETTING / LOCKED / CHART / RESULT 시간 변경
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoundSettingServiceImpl implements RoundSettingService {
    private final RoundSettingRepository roundSettingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void initDefaultSettings() {
        if (roundSettingRepository.findTopByOrderByIdDesc().isEmpty()) {
            createDefaultSettings();
            log.info("[RoundSettingService] 기본 설정 값 생성 완료");
        }
    }
    /**
     * 현재 라운드 설정 조회
     */
    @Transactional(readOnly = true)
    public RoundSettingResponseDTO getCurrentSettings() {
        return roundSettingRepository.findTopByOrderByIdDesc()
                .map(RoundSettingResponseDTO::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SETTING_REQUEST));
    }

    /**
     * 새로운 업데이트된 라운드 설정 저장
     */
    @Transactional
    public RoundSettingResponseDTO updateRoundSettings(RoundSettingRequestDTO request) {
        validateSettings(request);

        RoundSettingEntity currentSetting = roundSettingRepository.findTopByOrderByIdDesc()
                .orElseGet(RoundSettingEntity::new);

        currentSetting.setBettingTime(request.getBettingTime());
        currentSetting.setBettingLockTime(request.getBettingLockTime());
        currentSetting.setProcessingTime(request.getProcessingTime());
        currentSetting.setResultTime(request.getResultTime());

        RoundSettingEntity savedEntity = roundSettingRepository.save(currentSetting);
        log.info("[RoundSettingService] 새로운 라운드 설정이 저장되었습니다.");

        eventPublisher.publishEvent(new RoundSettingsUpdatedEvent(this));

        return RoundSettingResponseDTO.fromEntity(savedEntity);
    }

    /**
     * 라운드 설정값 유효성 검증
     */
    private void validateSettings(RoundSettingRequestDTO settings) {
        if (settings.getBettingTime() <= 0 ||
                settings.getBettingLockTime() <= 0 ||
                settings.getProcessingTime() <= 0 ||
                settings.getResultTime() <= 0) {
            throw new IllegalArgumentException("모든 설정값은 1초 이상이어야 합니다.");
        }
    }

    /**
     * 기본 설정 생성
     */
    private RoundSettingResponseDTO createDefaultSettings() {
        RoundSettingEntity defaultSettings = new RoundSettingEntity();
        defaultSettings.setBettingTime(15);
        defaultSettings.setBettingLockTime(5);
        defaultSettings.setProcessingTime(5);
        defaultSettings.setResultTime(5);

        RoundSettingEntity saved = roundSettingRepository.save(defaultSettings);
        return RoundSettingResponseDTO.fromEntity(saved);
    }
}