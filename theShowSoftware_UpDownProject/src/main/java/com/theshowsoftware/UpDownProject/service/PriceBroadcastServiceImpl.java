package com.theshowsoftware.UpDownProject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theshowsoftware.UpDownProject.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class PriceBroadcastServiceImpl implements PriceBroadcastService {
    private final PriceService priceService;
    private final SimpMessagingTemplate messagingTemplate;

    // 활성 세션 관리
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    // 데이터를 요청한 세션 관리 (실시간 데이터 요청 관리)
    private final Set<String> requestedSessions = ConcurrentHashMap.newKeySet();

    // 가격 수집 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 최근 수집한 가격
    private volatile BigDecimal latestPrice;

    public PriceBroadcastServiceImpl(PriceService priceService,
                                     SimpMessagingTemplate messagingTemplate) {
        this.priceService = priceService;
        this.messagingTemplate = messagingTemplate;

        // 0.5초마다 API에서 최신 가격 수집
        scheduler.scheduleAtFixedRate(this::collectPrice, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 세션을 활성화하고 활성 세션에 추가
     */
    @Override
    public void startBroadcast(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            if (activeSessions.add(sessionId)) {
                log.info("[WebSocket] 세션 활성화 성공 - sessionId={}, 현재 활성 세션 수={}", sessionId, activeSessions.size());
            } else {
                log.warn("[WebSocket] 이미 활성화된 세션 - sessionId={}", sessionId);
            }
        } else {
            log.warn("[WebSocket] 유효하지 않은 세션 ID로 인해 등록 실패");
        }
    }

    /**
     * 세션을 비활성화하고 요청 세션에서 제거
     * @param sessionId
     */
    @Override
    public void stopBroadcast(String sessionId) {
        // 활성 세션 및 요청 세션에서 제거
        boolean wasRequested = requestedSessions.remove(sessionId);
        boolean wasActive = activeSessions.remove(sessionId);

        if (wasActive || wasRequested) {
            log.info("[WebSocket] 세션 비활성화 또는 요청 해제 - sessionId={}, 남은 세션 수={}", sessionId, activeSessions.size());
        } else {
            log.warn("[WebSocket] 세션 제거 요청 받았지만 존재하지 않음 - sessionId={}", sessionId);
        }
    }

    /**
     * 지난 초 동안의 가격 정보를 요청한 세션에게 전송
     */
    @Override
    public void sendHistoryPrices(String sessionId) {
        try {

            Map<String, Object> priceData = priceService.getHistoryPrices();

            String jsonMessage = new ObjectMapper().writeValueAsString(priceData);

            messagingTemplate.convertAndSendToUser(sessionId, "/price/history", jsonMessage);
            log.info("[WebSocket] 최근 120초 가격 전송 완료 - sessionId={}, 데이터 크기={}", sessionId, ((List<?>) priceData.get("prices")).size());
        } catch (Exception e) {
            log.error("[WebSocket] 최근 120초 가격 데이터 전송 실패 - sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    /**
     * 실시간 가격 데이터 요청
     */
    @Override
    public void requestLivePrices(String sessionId) {
        if (!activeSessions.contains(sessionId)) {
            log.warn("[WebSocket] 실시간 데이터 요청받았지만 세션이 활성화되지 않음 - sessionId={}", sessionId);
            return;
        }

        if (requestedSessions.add(sessionId)) {
            log.info("[WebSocket] 실시간 가격 데이터 요청 활성화 - sessionId={}", sessionId);
        } else {
            log.warn("[WebSocket] 이미 실시간 데이터가 요청된 세션 - sessionId={}", sessionId);
        }
    }

    /**
     * 실시간 데이터 수집 및 브로드캐스트
     */
    private void collectPrice() {
        try {
            latestPrice = priceService.fetchCurrentPrice(false);
            log.debug("[exchange API] 최근 가격 수집 - price={}", latestPrice);

            // 요청한 세션들에게만 최신 가격 데이터를 전송
            broadcastToRequestedSessions();

        } catch (DataAccessException dae) {
            log.error("[PriceBroadcastService] DB 접근 실패 - {}", dae.getMessage());
        } catch (Exception e) {
            log.error("[PriceBroadcastService] 가격 수집 실패 - error={}", e.getMessage());
        }
    }

    /**
     * 실시간 가격 데이터를 요청한 세션들에게 전송
     */
    private void broadcastToRequestedSessions() {
        if (latestPrice == null || requestedSessions.isEmpty()) {
            log.debug("[WebSocket] 요청된 세션이 없거나 데이터가 유효하지 않음");
            return;
        }
        try {
            String currentPrices = priceService.getCurrentPrices(latestPrice);

            requestedSessions.parallelStream().forEach(sessionId -> {
                try {
                    messagingTemplate.convertAndSendToUser(sessionId, "/price/current", currentPrices);

                    log.info("[WebSocket] 실시간 가격 전송 - sessionId={}, price={}", sessionId, latestPrice);
                } catch (Exception e) {
                    log.error("[WebSocket] 실시간 가격 전송 실패 - sessionId={}, error={}", sessionId, e.getMessage());
                }
            });
        } catch (CustomException e) {
            log.error("[WebSocket] 실시간 가격 데이터 직렬화 실패 - error={}", e.getMessage());
        }
    }

    /**
     * 실시간 전송된 가격 캐싱
     */
    @Override
    public BigDecimal getCachedLatestPrice() {
        return latestPrice;
    }
}