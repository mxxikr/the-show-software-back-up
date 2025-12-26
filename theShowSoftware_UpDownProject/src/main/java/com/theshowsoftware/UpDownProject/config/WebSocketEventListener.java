package com.theshowsoftware.UpDownProject.config;

import com.theshowsoftware.UpDownProject.service.PriceBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final PriceBroadcastService priceBroadcasterService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            priceBroadcasterService.stopBroadcast(sessionId);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        if (sessionId != null && destination != null) {
            priceBroadcasterService.startBroadcast(sessionId); // 활성 세션에 등록
            log.info("[WebSocket] 새 구독 요청 - sessionId={}, destination={}", sessionId, destination);
        } else {
            log.warn("[WebSocket] 구독 요청 실패 - sessionId={}, destination={}", sessionId, destination);
        }
    }
}