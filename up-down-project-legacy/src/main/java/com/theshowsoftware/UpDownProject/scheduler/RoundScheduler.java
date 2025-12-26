package com.theshowsoftware.UpDownProject.scheduler;

import com.theshowsoftware.UpDownProject.dto.RoundResponseDTO;
import com.theshowsoftware.UpDownProject.dto.RoundSettingResponseDTO;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import com.theshowsoftware.UpDownProject.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 라운드를 주기적으로 생성/관리하는 스케줄러
 * - 매일 자정에 라운드 번호를 1로 초기화하고 새로운 라운드를 시작
 * - 설정값(베팅/잠금/게임진행/결과) 합계에 맞춰 일정 간격(기본 30초)으로 라운드를 자동 생성
 * - update_round_settings API 호출 시 스케줄 재등록
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoundScheduler {
    private final RoundService roundService;
    private final RoundBroadCastService roundBroadCastService;
    private final PriceService priceService;
    private final PriceBroadcastService priceBroadcastService;
    private final RoundSettingService roundSettingService;

    private ScheduledExecutorService roundScheduler;
    private final ReentrantLock settingsLock = new ReentrantLock();

    private volatile RoundSettingResponseDTO cachedSettings;
    private static final int MAX_RETRY = 3;

    /**
     * 애플리케이션 시작 시 초기 스케줄링 등록
     */
    @PostConstruct
    public void initialize() {
        refreshCachedSettings();
        scheduleNextRound();
        log.info("[RoundScheduler] 스케줄러 초기화 완료");
    }

    /**
     * 애플리케이션 종료 시 스케줄러 종료
     */
    @PreDestroy
    public void shutdownScheduler() {
        if (roundScheduler != null && !roundScheduler.isShutdown()) {
            roundScheduler.shutdownNow();
            log.info("[RoundScheduler] 스케줄러 종료 완료");
        }
    }

    /**
     * 매일 자정에 라운드 번호 초기화 및 첫 라운드 리셋
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void resetDailyRounds() {
        log.info("[RoundScheduler] 매일 자정 라운드 초기화 시작...");
        settingsLock.lock();
        try {
            roundService.startNewDay(); // 전날 미완료 라운드 처리
            refreshCachedSettings();
            scheduleNextRound();        // 새 스케줄러 시작
            log.info("[RoundScheduler] 매일 자정 라운드 초기화 및 새 스케줄 시작 완료");
        } catch(Exception e) {
            log.error("[RoundScheduler] 자정 라운드 초기화 중 오류 발생", e);
        } finally {
            settingsLock.unlock();
        }
    }

    /**
     * 3개월 이상된 라운드 삭제
     */
    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul") // 매일 자정 10분 후 실행
    public void deleteOldRounds() {
        try {
            LocalDate cutoffDate = LocalDate.now().minusMonths(3);
//            LocalDate cutoffDate = LocalDate.now(); // TODO: 테스트 용 하루 전 데이터만 삭제

            roundService.deleteRoundsBefore(cutoffDate);
            log.info("[RoundScheduler] {} 이전 라운드 삭제 작업 완료", cutoffDate);
        } catch (Exception e) {
            log.error("[RoundScheduler] 라운드 삭제 중 오류 발생", e);
        }
    }

    /**
     * 라운드 설정 업데이트 시 호출되어 스케줄러를 재시작
     */
    public void onRoundSettingsUpdated() {
        log.info("[RoundScheduler] 라운드 설정 업데이트 감지. 스케줄러 재시작 시도...");
        settingsLock.lock();
        try {
            refreshCachedSettings(); // 새 설정 캐싱
            log.info("[RoundScheduler] 라운드 설정 업데이트 반영 및 스케줄러 재시작 완료");
        } finally {
            settingsLock.unlock();
        }
    }

    /**
     * 스케줄 초기화 및 설정 기반 스케줄링 등록
     */
    public synchronized void scheduleNextRound() {
        stopCurrentScheduler(); // 현재 스케줄러 중지

        try {
            // 스레드 풀 초기화
            roundScheduler = Executors.newSingleThreadScheduledExecutor();
            int interval = calculateRoundInterval(); // 캐시된 설정으로 라운드 간격 계산

            // 즉시 첫 라운드를 시작하고, 이후 interval 간격으로 반복 실행
            roundScheduler.scheduleAtFixedRate(this::createAndStartRound, 0, interval, TimeUnit.SECONDS);

            log.info("[RoundScheduler] 새 라운드 스케줄 등록 완료 (간격: {}초)", interval);
        } catch (Exception e) {
            log.error("[RoundScheduler] 스케줄 등록 중 오류 발생", e);
            stopCurrentScheduler();
        }
    }

    /**
     * 현재 스케줄러 중단
     */
    private void stopCurrentScheduler() {
        if (roundScheduler != null && !roundScheduler.isShutdown()) {
            log.info("[RoundScheduler] 기존 스케줄러 중단 시도...");
            roundScheduler.shutdownNow(); // 즉시 중단 요청
            try {
                // 중단 완료 대기
                if (!roundScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("[RoundScheduler] 스케줄러 중단 시간 초과. 강제 종료 시도");
                } else {
                    log.info("[RoundScheduler] 기존 스케줄러 중단 완료");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                log.error("[RoundScheduler] 스케줄러 종료 대기 중 인터럽트 발생", e);
            }
        }
        roundScheduler = null; // 참조 제거
    }

    /**
     * 새 라운드 생성 및 시작
     */
    void createAndStartRound() {
        int retryCount = 0;
        boolean success = false;

        while (retryCount < MAX_RETRY && !success) {
            try {
                // 새 라운드 생성
                RoundResponseDTO newRound = roundService.createNextRound();
                log.info("[RoundScheduler] 새 라운드 생성 성공 - ID: {}, Num: {}, 상태: {}", newRound.getId(), newRound.getRoundNum(), RoundStatus.BETTING);

                // 라운드 processingTime 시작/종료 시간 예측 및 캐싱 (클라 전송용)
                LocalDateTime roundStartTime = LocalDateTime.now();
                RoundSettingResponseDTO settings = cachedSettings; // 캐시된 설정 사용
                LocalDateTime expectedProcessingStartTime = roundStartTime.plusSeconds(settings.getBettingTime() + settings.getBettingLockTime());
                LocalDateTime expectedProcessingEndTime = expectedProcessingStartTime.plusSeconds(settings.getProcessingTime());

                // 캐싱 함수 호출
                roundBroadCastService.cacheRoundProcessingTime(newRound.getId(), expectedProcessingStartTime, expectedProcessingEndTime);

                // 라운드 단계별 스케줄 등록
                scheduleRoundPhases(newRound.getId());
                success = true; // 성공 플래그 설정
            } catch  (Exception e) {
                retryCount++;
                log.error("[RoundScheduler] 라운드 생성 중 예기치 않은 오류 발생 (재시도 {}/{})", retryCount, MAX_RETRY, e);
            }
        }

        if (!success) {
            log.error("[RoundScheduler] 라운드 생성 최대 재시도 ({}) 실패. 스케줄러 확인 필요.", MAX_RETRY);
            stopCurrentScheduler();
        }
    }


    /**
     * 각 라운드 단계별 스케줄링 등록
     */
    void scheduleRoundPhases(Long roundId) {
        try {
            RoundSettingResponseDTO settings = roundSettingService.getCurrentSettings();

            long bettingEndTime = settings.getBettingTime();
            long lockedEndTime = bettingEndTime + settings.getBettingLockTime();
            long processingEndTime = lockedEndTime + settings.getProcessingTime();
            long finishedTransitionTime = processingEndTime; // FINISHED 상태로 변경되는 시점
            long roundEndTime = finishedTransitionTime + settings.getResultTime(); // round_end_time 기록 시점

            roundScheduler.schedule(() -> updateRoundStatus(roundId, RoundStatus.LOCKED), bettingEndTime, TimeUnit.SECONDS);
            roundScheduler.schedule(() -> updateRoundStatus(roundId, RoundStatus.PROCESSING), lockedEndTime, TimeUnit.SECONDS);
            roundScheduler.schedule(() -> updateRoundStatus(roundId, RoundStatus.FINISHED), finishedTransitionTime, TimeUnit.SECONDS);

            // 총 라운드 종료 시점에 roundEndTime 업데이트
            roundScheduler.schedule(() -> roundService.finalizeRoundEndTime(roundId), roundEndTime, TimeUnit.SECONDS);

            log.info("[RoundScheduler] 라운드 ID: {} 단계별 스케줄 등록 완료. LOCKED 전환: {}초, PROCESSING 전환: {}초, FINISHED 전환: {}초, roundEndTime 설정 예정: {}초",
                    roundId, bettingEndTime, lockedEndTime, finishedTransitionTime, roundEndTime);

        } catch (Exception e) {
        log.error("[RoundScheduler] 라운드 ID: {} 단계별 스케줄 등록 중 오류 발생", roundId, e);
        }
    }

    /**
     * 라운드 상태 업데이트 로직
     */
    void updateRoundStatus(Long roundId, RoundStatus nextStatus) {
        try {
            log.debug("[RoundScheduler] 라운드 {} 상태 변경 시도 -> {}", roundId, nextStatus);
            BigDecimal startPrice = null;
            BigDecimal endPrice = null;

            if (nextStatus == RoundStatus.LOCKED) { // 라운드 상태 Locked 처리
                log.debug("[RoundScheduler] 라운드 {} - LOCKED 상태로 전환", roundId);
            } else if (nextStatus == RoundStatus.PROCESSING) { // 라운드 상태 Processing 처리
//                startPrice = priceService.fetchCurrentPrice(true); // 게임 진행 시작 지점 가격 조회
                startPrice = priceBroadcastService.getCachedLatestPrice(); // 게임 진행 시작 지점 가격 조회 캐싱 값 사용

                roundBroadCastService.sendRoundProcessingStartPrice(roundId, startPrice); // 클라 전송용

                if (startPrice == null) {
                    log.error("[RoundScheduler] 라운드 {} PROCESSING 진입 실패: 시작 가격 조회 실패", roundId);
                    return;
                }
                log.debug("[RoundScheduler] 라운드 {} 시작 가격 조회 성공: {}", roundId, startPrice);
            } else if (nextStatus == RoundStatus.FINISHED) {
//                endPrice = priceService.fetchCurrentPrice(true); // 게임 진행 종료 시점 가격 조회
                endPrice = priceBroadcastService.getCachedLatestPrice(); // 게임 진행 종료 지점 가격 조회 캐싱 값 사용

                roundBroadCastService.sendRoundProcessingEndPrice(roundId, endPrice); // 클라 전송용

                if (endPrice == null) {
                    log.error("[RoundScheduler] 라운드 {} FINISHED 진입 실패: 종료 가격 조회 실패", roundId);
                    return;
                }
                log.info("[RoundScheduler] 라운드 {} 종료 가격 조회 성공: {}", roundId, endPrice);
            }

            roundService.updateRound(roundId, nextStatus, startPrice, endPrice);
            log.debug("[RoundScheduler] 라운드 {} 상태 변경 완료 -> {}", roundId, nextStatus);

        } catch (Exception e) {
            log.error("[RoundScheduler] 라운드 ID: {} 상태 {}로 변경 중 오류 발생", roundId, nextStatus, e);
        }
    }

    /**
     * 라운드 스케줄 간격 계산
     */
    private int calculateRoundInterval() {
        if (cachedSettings == null) {
            log.warn("[RoundScheduler] 라운드 설정이 캐시되지 않았습니다. 기본값 또는 재조회를 시도합니다.");
            refreshCachedSettings(); // 캐시 재시도
            if (cachedSettings == null) {
                log.error("[RoundScheduler] 설정을 로드할 수 없어 기본 간격을 사용합니다.");
                return 30; // 설정 로드 실패 시 기본값
            }
        }
        return cachedSettings.getBettingTime() +
            cachedSettings.getBettingLockTime() +
            cachedSettings.getProcessingTime() +
            cachedSettings.getResultTime();
    }

    /**
     * DB에서 최신 라운드 설정을 조회하여 캐시에 저장
     */
    private void refreshCachedSettings() {
        try {
            RoundSettingResponseDTO settings = roundSettingService.getCurrentSettings();
            // 설정 값 유효성 검사
            if (settings.getBettingTime() < 0 || settings.getBettingLockTime() < 0 ||
                    settings.getProcessingTime() < 0 || settings.getResultTime() < 0) {
                log.error("[RoundScheduler] 라운드 설정 값에 음수가 포함되어 있습니다: {}", settings);
                throw new IllegalArgumentException("라운드 설정 시간 값은 음수일 수 없습니다.");
            }
            cachedSettings = settings;
            log.info("[RoundScheduler] 라운드 설정 값 캐싱 완료: {}", cachedSettings);
        } catch (Exception e) {
            log.error("[RoundScheduler] 라운드 설정 값 조회 또는 캐싱 중 오류 발생", e);
            if (cachedSettings == null) {
                log.warn("[RoundScheduler] 캐시된 설정이 없어 스케줄링에 문제가 발생할 수 있습니다.");
                cachedSettings = new RoundSettingResponseDTO(15, 5, 5, 5); // 기본 값
            }
        }
    }
}