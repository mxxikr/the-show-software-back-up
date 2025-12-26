package com.theshowsoftware.UpDownProject.controller;

import com.theshowsoftware.UpDownProject.service.PriceBroadcastService;
import com.theshowsoftware.UpDownProject.service.RoundBroadCastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastController {
    private final PriceBroadcastService priceBroadcasterService;
    private final RoundBroadCastService roundBroadCasterService;

    /**
     * 클라이언트가 /pub/price/history로 요청을 보내면 최근 60초 가격 데이터 전송
     */
    @MessageMapping("/price/history")
    public void sendPriceHistory(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        priceBroadcasterService.sendHistoryPrices(sessionId);
    }

    /**
     * 클라이언트가 /pub/price/current로 요청을 보내면 실시간 데이터 활성화
     */
    @MessageMapping("/price/current")
    public void subscribeToPriceUpdates(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        priceBroadcasterService.requestLivePrices(sessionId);
    }

    /**
     * 클라이언트가 /pub/round/info로 요청할 경우 라운드 정보를 전송
     */
    @MessageMapping("/round/info")
    public void getRoundInfo(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        roundBroadCasterService.sendRoundInfo(sessionId);
    }

    /**
     * 클라이언트가 /pub/round/prices/start 요청 시 세션 등록만 진행
     */
    @MessageMapping("/round/prices/start")
    public void registerRoundStartPriceListener(SimpMessageHeaderAccessor headerAccessor) {
        roundBroadCasterService.registerSessionForStartPrice(headerAccessor.getSessionId());
    }

    /**
     * 클라이언트가 /pub/round/prices/end 요청 시 세션 등록만 진행
     */
    @MessageMapping("/round/prices/end")
    public void registerRoundEndPriceListener(SimpMessageHeaderAccessor headerAccessor) {
        roundBroadCasterService.registerSessionForEndPrice(headerAccessor.getSessionId());
    }
}