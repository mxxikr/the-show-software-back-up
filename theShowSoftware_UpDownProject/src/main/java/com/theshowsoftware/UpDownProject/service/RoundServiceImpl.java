package com.theshowsoftware.UpDownProject.service;

import com.theshowsoftware.UpDownProject.domain.RoundEntity;
import com.theshowsoftware.UpDownProject.dto.RoundResponseDTO;
import com.theshowsoftware.UpDownProject.enums.ResultType;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import com.theshowsoftware.UpDownProject.enums.ErrorCode;
import com.theshowsoftware.UpDownProject.repository.RoundRepository;
import com.theshowsoftware.UpDownProject.exception.CustomException;

import com.theshowsoftware.UpDownProject.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundServiceImpl implements RoundService {
    private final RoundRepository roundRepository;
    private final ReentrantLock lock = new ReentrantLock();

    //---------------------------------------------------------
    // 라운드 정보 조회
    // --------------------------------------------------------
    /**
     * 현재 진행 중인 라운드 조회
     */
    @Override
    public RoundResponseDTO getCurrentRound() {
        // ID 내림차순으로 가장 첫번째 라운드를 가져옴
        return roundRepository.findTopByOrderByIdDesc()
                .map(RoundResponseDTO::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }

    /**
     * 특정 날짜와 라운드 번호로 라운드 조회
     */
    @Override
    public RoundResponseDTO getRound(LocalDate roundDate, Integer roundNum) {
        return roundRepository.findByRoundDateAndRoundNum(roundDate, roundNum)
                .map(RoundResponseDTO::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }

    /**
     * round_status가 FINISHED인 가장 최신 라운드 조회
     */
    @Override
    public RoundResponseDTO getLatestFinishedRound() {
        return roundRepository.findTopByRoundStatusOrderByIdDesc(RoundStatus.FINISHED)
                .map(RoundResponseDTO::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }

    //---------------------------------------------------------
    // 라운드 관리
    // --------------------------------------------------------
    /**
     * 새로운 라운드 생성 (BETTING 상태로 시작) 및 이전 라운드 종료 시간 설정
     */
    @Transactional
    @Override
    public RoundResponseDTO createNextRound() {
        lock.lock();
        try {
            // ------ 이전 라운드 종료 시간 설정 ------
            try {
                Optional<RoundEntity> latestRoundOpt = roundRepository.findTopByOrderByIdDesc();

                if (latestRoundOpt.isPresent()) {
                    RoundEntity latestRound = latestRoundOpt.get();
                    if (latestRound.getRoundStatus() == RoundStatus.FINISHED && latestRound.getRoundEndTime() == null) {
                        latestRound.setRoundEndTime(TimeFormatUtil.format(LocalDateTime.now()));
                        roundRepository.save(latestRound);
                        log.debug("[RoundService] 이전 라운드 {} 종료 시간 설정 완료: {}", latestRound.getId(), latestRound.getRoundEndTime());
                    } else if (latestRound.getRoundEndTime() != null) {
                        log.debug("[RoundService] 이전 라운드 {}의 종료 시간은 이미 설정됨: {}", latestRound.getId(), latestRound.getRoundEndTime());
                    } else {
                        log.warn("[RoundService] 이전 라운드 {}가 FINISHED 상태가 아니므로({}), 종료 시간을 설정하지 않습니다.", latestRound.getId(), latestRound.getRoundStatus());
                    }
                } else {
                    log.info("[RoundService] 첫 라운드이므로 종료 시간을 설정하지 않습니다.");
                }
            } catch (Exception e) {
                log.error("[RoundService] 이전 라운드 종료 처리 중 오류 발생. 새 라운드 생성은 계속 진행됩니다.", e);
            }

            // ------ 새 라운드 생성 ------
            LocalDate today = LocalDate.now();
            Integer nextRoundNum = roundRepository.getNextRoundNumber(today)
                    .orElseThrow(() -> {
                        log.error("[RoundService] Fatal: Could not determine next round number for date: {}", today);
                        return new CustomException(ErrorCode.ROUND_NUMBER_CREATE_FAIL);
                    });

            boolean exists = roundRepository.existsByRoundDateAndRoundNum(today, nextRoundNum);
            if (exists) {
                log.warn("[RoundService] 중복 라운드 생성 시도 감지: Date={}, Num={}", today, nextRoundNum);
                throw new CustomException(ErrorCode.DUPLICATE_ROUND);
            }

            RoundEntity newRound = RoundEntity.builder()
                    .roundNum(nextRoundNum)
                    .roundDate(today)
                    .roundStatus(RoundStatus.BETTING)
                    .roundStartTime(TimeFormatUtil.format(LocalDateTime.now())) // 라운드 시작 시간
                    .build();

            RoundEntity savedRound = roundRepository.save(newRound);
            log.info("[RoundService] 새 라운드 생성 완료 (BETTING 시작) - ID: {}, Num: {}, 시작시간: {}", savedRound.getId(), savedRound.getRoundNum(), savedRound.getRoundStartTime());
            return RoundResponseDTO.fromEntity(savedRound);

        } finally {
            lock.unlock(); // try 블록 전체에 대한 finally에서 unlock 보장
        }
    }

    /**
     * 이전 라운드의 종료 시간(round_end_time)을 설정
     * 다음 라운드가 시작되기 직전에 호출
     */
    @Transactional
    @Override
    public void finalizeRoundEndTime(Long roundId) {
        try {
            RoundEntity round = getValidRound(roundId);
            if (round.getRoundEndTime() == null) {
                round.setRoundEndTime(TimeFormatUtil.format(LocalDateTime.now())); // 현재 시간을 라운드 종료 시간으로 설정
                roundRepository.save(round);
                log.debug("[RoundService] 라운드 {} 종료 시간 설정 완료: {}", roundId, round.getRoundEndTime());
            } else {
                log.warn("[RoundService] 라운드 {}의 종료 시간이 이미 설정되어 있습니다: {}", roundId, round.getRoundEndTime());
            }
        } catch (CustomException e) {
            log.error("[RoundService] 라운드 종료 시간 설정 실패 (라운드 {} 찾을 수 없음): {}", roundId, e.getMessage());
        } catch (Exception e) {
            log.error("[RoundService] 라운드 {} 종료 시간 설정 중 예외 발생: {}", roundId, e.getMessage(), e);
        }
    }

    /**
     * 라운드 상태 업데이트 및 관련 데이터 처리
     * - 상태 변경 (BETTING -> LOCKED -> PROCESSING -> FINISHED)
     * - PROCESSING 진입 시 game_start_time 기록
     * - FINISHED 진입 시 game_end_time 기록, 결과 처리 및 저장
     */
    @Transactional
    @Override
    public void updateRound(Long roundId, RoundStatus roundStatus, BigDecimal gameStartPrice, BigDecimal gameEndPrice) {
        RoundEntity round = getValidRound(roundId);

        switch (roundStatus) {
            case LOCKED:
                log.debug("[RoundService] 라운드 {} - LOCKED 상태로 전환.", roundId);
                break;
            case PROCESSING: {
                if (gameStartPrice == null) {
                    log.error("[RoundService] 라운드 {} PROCESSING 진입 시 시작가 누락", roundId);
                    throw new CustomException(ErrorCode.PRICE_FETCH_FAILED);
                }
                round.setGameStartPrice(gameStartPrice);
                round.setGameStartTime(TimeFormatUtil.format(LocalDateTime.now())); // game_start_time 설정
                log.debug("[RoundService] 라운드 {} - PROCESSING 상태로 전환, 시작가: {}", round.getRoundNum(), gameStartPrice);
                break;
            }
            case FINISHED: {
                if (gameEndPrice == null) {
                    log.error("[RoundService] 라운드 {} FINISHED 진입 시 종료가 누락", roundId);
                    throw new CustomException(ErrorCode.PRICE_FETCH_FAILED);
                }

                if (round.getGameStartPrice() == null || round.getGameStartTime() == null) {
                    log.error("[RoundService] 라운드 {} FINISHED 처리 불가 - 시작 정보 누락 (시작가: {}, 시작시간: {})",
                            roundId, round.getGameStartPrice(), round.getGameStartTime());
                    throw new CustomException(ErrorCode.ROUND_PRICE_NOT_FOUND);
                }

                round.setGameEndPrice(gameEndPrice);
                round.setGameEndTime(TimeFormatUtil.format(LocalDateTime.now())); // game_end_time 설정

                // 결과 계산
                if (round.getGameStartPrice() == null) {
                    throw new CustomException(ErrorCode.ROUND_PRICE_NOT_FOUND);
                }
                ResultType result = calculateResult(round.getGameStartPrice(), gameEndPrice);
                round.setRoundResult(result);
                log.debug("[RoundService] 라운드 {} - FINISHED 상태 완료, 결과: {}, 시작가: {}, 종료가: {}, 게임 시작 시간: {}, 게임 종료 시간: {}, 라운드 종료 시간: {}",
                        roundId, result, round.getGameStartPrice(), gameEndPrice, round.getGameStartTime(), round.getGameEndTime(), round.getRoundEndTime());
                break;
            }
            default:
                log.info("[RoundService] 라운드 {} - 상태 변경: {}", round.getRoundNum(), roundStatus);
        }
        round.setRoundStatus(roundStatus);
        roundRepository.save(round);
    }

    /**
     * 매일 자정 초기화 메서드
     */
    @Transactional
    @Override
    public void startNewDay() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<RoundEntity> unfinishedRounds = roundRepository.findAllByRoundDateAndRoundStatusNot(yesterday, RoundStatus.FINISHED);

        // 미완료 라운드 처리
        unfinishedRounds.forEach(round -> {
            log.warn("[RoundService] 전날 미완료 라운드 강제 종료 처리: ID={}, 상태={}", round.getId(), round.getRoundStatus());
            if (round.getGameEndTime() == null) {
                round.setGameEndTime(TimeFormatUtil.format(round.getRoundDate().atTime(23, 59, 59)));
            }
            if(round.getRoundEndTime() == null) {
                round.setRoundEndTime(TimeFormatUtil.format(round.getRoundDate().atTime(23, 59, 59)));
            }
            if (round.getRoundResult() == null) {
                round.setRoundResult(ResultType.NONE); // 결과는 NONE으로 처리
            }
            round.setRoundStatus(RoundStatus.FINISHED);
        });

        if (!unfinishedRounds.isEmpty()) {
            roundRepository.saveAll(unfinishedRounds);
            log.info("[RoundService] 전날 미완료 라운드 {}건 FINISHED 처리 완료", unfinishedRounds.size());
        } else {
            log.info("[RoundService] 전날 미완료된 라운드가 없습니다.");
        }
    }

    /**
     * 특정 날짜 이전의 라운드 삭제
     */
    @Override
    @Transactional
    public void deleteRoundsBefore(LocalDate cutoff) {
        try {
            long count = roundRepository.countByRoundDateBefore(cutoff);
            if (count > 0) {
                roundRepository.deleteAllByRoundDateBefore(cutoff); // 기준 날짜 이전의 모든 라운드 삭제
                log.info("[RoundService] {} 이전 라운드 {}건 삭제 완료.", cutoff, count);
            } else {
                log.info("[RoundService] {} 이전 삭제할 라운드가 없습니다.", cutoff);
            }
        } catch (Exception e) {
            log.error("[RoundService] 이전 라운드 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 게임 결과 계산 로직
     */
    private ResultType calculateResult(BigDecimal startPrice, BigDecimal endPrice) {
        if (startPrice == null || endPrice == null) {
            log.error("[RoundService] 결과 계산 불가 - 가격 정보 누락 (시작가: {}, 종료가: {})", startPrice, endPrice);
            return ResultType.NONE;
        }
        int comparison = endPrice.compareTo(startPrice);
        if (comparison > 0) return ResultType.UP;
        else if (comparison < 0) return ResultType.DOWN;
        else return ResultType.SAME;
    }

    private RoundEntity getValidRound(Long roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }
}