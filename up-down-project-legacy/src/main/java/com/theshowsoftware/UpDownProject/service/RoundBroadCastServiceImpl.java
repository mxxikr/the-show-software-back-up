package com.theshowsoftware.UpDownProject.service;

import com.theshowsoftware.UpDownProject.dto.RoundEndPriceResponseDTO;
import com.theshowsoftware.UpDownProject.dto.RoundStartPriceResponseDTO;
import com.theshowsoftware.UpDownProject.dto.RoundTimestampsResponseDTO;
import com.theshowsoftware.UpDownProject.enums.ErrorCode;
import com.theshowsoftware.UpDownProject.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundBroadCastServiceImpl implements RoundBroadCastService {
    private final SimpMessagingTemplate messagingTemplate;
    private volatile RoundTimestampsResponseDTO roundTimestampsResponseDTO;
    private final ReentrantLock cacheLock = new ReentrantLock();

    // 시작 가격 요청 대기 세션 관리
    private final Set<String> waitingStartPriceSessions = ConcurrentHashMap.newKeySet();
    private final Set<String> waitingEndPriceSessions = ConcurrentHashMap.newKeySet();

    //---------------------------------------------------------
    // 클라 데이터 전송용
    // --------------------------------------------------------
    /**
     * 클라이언트에게 라운드 정보를 전송
     */
    @Override
    public void sendRoundInfo(String sessionId) {
        // 현재 캐싱된 라운드 정보 가져오기
        RoundTimestampsResponseDTO roundTimestampsResponseDTO = this.roundTimestampsResponseDTO;
        try {
            if (roundTimestampsResponseDTO == null) {
                log.warn("[RoundService] 현재 진행 중인 라운드 정보가 없습니다.");
                messagingTemplate.convertAndSendToUser(sessionId, "/round/info", ErrorCode.ROUND_NOT_ONGOING);
                return;
            }

            // 서버의 현재 시간 추가 (클라이언트 시간 동기화 용도)
            // gameStartTimestamp와 gameEndTimestamp는 캐시된 값을 사용
            RoundTimestampsResponseDTO updatedDTO = RoundTimestampsResponseDTO.builder()
                    .roundId(roundTimestampsResponseDTO.getRoundId())
                    .serverTimestamp(TimeFormatUtil.getCurrentTimestamp()) // 현재 서버 시간
                    .gameStartTimestamp(roundTimestampsResponseDTO.getGameStartTimestamp()) // 캐시된 게임 시작 시간 (PROCESSING 시작 예상 시간)
                    .gameEndTimestamp(roundTimestampsResponseDTO.getGameEndTimestamp())     // 캐시된 게임 종료 시간 (FINISHED 시작 예상 시간)
                    .build();

            messagingTemplate.convertAndSendToUser(sessionId, "/round/info", updatedDTO);
            log.debug("[RoundService] 세션 {}에 라운드 정보를 전송했습니다: {}", sessionId, updatedDTO);

        } catch (Exception e) {
            log.error("[RoundService] 라운드 정보 전송 중 오류 발생 (세션: {}): {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 라운드가 시작될 때 예상되는 gameStartTimestamp와 gameEndTimestamp 정보를 캐싱
     *
     * @param roundId 라운드 ID
     * @param gameStartTime 예상되는 게임 시작 시간 (PROCESSING 상태 진입 시점)
     * @param gameEndTime 예상되는 게임 종료 시간 (FINISHED 상태 진입 시점)
     */
    @Override
    public void cacheRoundProcessingTime(Long roundId, LocalDateTime gameStartTime, LocalDateTime gameEndTime) {
        if (cacheLock.tryLock()) {
            try {
                // 캐시에 예상 시간 정보 저장
                roundTimestampsResponseDTO = RoundTimestampsResponseDTO.builder()
                        .roundId(roundId)
                        .serverTimestamp(TimeFormatUtil.getCurrentTimestamp())
                        .gameStartTimestamp(gameStartTime.toInstant(ZoneOffset.UTC).toEpochMilli()) // 예상 게임 시작 시간
                        .gameEndTimestamp(gameEndTime.toInstant(ZoneOffset.UTC).toEpochMilli())     // 예상 게임 종료 시간
                        .build();

                log.debug("[RoundService] 라운드 타임스탬프 캐싱 완료 - ID: {}, 예상 게임 시작 시간: {}, 예상 게임 종료 시간: {}",
                        roundId, gameStartTime, gameEndTime);
            } catch (Exception e) {
                log.error("[RoundService] 라운드 타임스탬프 캐싱 중 오류 발생: {}", e.getMessage(), e);
            } finally {
                cacheLock.unlock();
            }
        } else {
            log.warn("[RoundService] 캐시 락 획득 실패 - 라운드 ID: {}", roundId);
        }
    }

    /**
     * 클라이언트가 시작 가격 요청 시 캐시 가격 전송이 아닌 등록만 진행
    **/
    @Override
    public void registerSessionForStartPrice(String sessionId) {
        waitingStartPriceSessions.add(sessionId);
        log.info("세션 {} 시작 가격 요청 등록 완료", sessionId);
    }

    /**
     * 클라이언트가 종료 가격 요청 시 캐시 가격 전송이 아닌 등록만 진행
     */
    @Override
    public void registerSessionForEndPrice(String sessionId) {
        waitingEndPriceSessions.add(sessionId);
        log.info("세션 {} 종료 가격 요청 등록 완료", sessionId);
    }

    /**
     *  실제 게임 진행 시작 가격 업데이트 발생 시 호출
     */
    @Override
    public void sendRoundProcessingStartPrice(Long roundId, BigDecimal gameStartPrice) {
        RoundStartPriceResponseDTO priceInfo = RoundStartPriceResponseDTO.builder()
                .roundId(roundId)
                .gameStartPrice(gameStartPrice)
                .build();

        waitingStartPriceSessions.parallelStream()
                .forEach(sessionId ->
                        messagingTemplate.convertAndSendToUser(sessionId, "/round/prices/start", priceInfo)
                );
        log.info("대기 중인 모든 세션에 시작 가격 발송 완료: {}", priceInfo);

        waitingStartPriceSessions.clear(); // 발송 후 리스트 초기화
    }

    /**
     *  실제 게임 진행 종료 가격 업데이트 발생 시 호출
     */
    @Override
    public void sendRoundProcessingEndPrice(Long roundId, BigDecimal gameEndPrice) {
        RoundEndPriceResponseDTO priceInfo = RoundEndPriceResponseDTO.builder()
                .roundId(roundId)
                .gameEndPrice(gameEndPrice)
                .build();
        waitingStartPriceSessions.parallelStream()
                .forEach(sessionId ->
                    messagingTemplate.convertAndSendToUser(sessionId, "/round/prices/end", priceInfo)
        );
        log.info("대기 중인 모든 세션에 종료 가격 발송 완료: {}", priceInfo);

        waitingEndPriceSessions.clear(); // 발송 후 리스트 초기화
    }
}
