package com.theshowsoftware.ChatServer.service;


import com.theshowsoftware.ChatServer.enums.SymbolType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PacketSenderService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 틱 데이터를 전송
     */
    public void sendTickDataToUser(SymbolType symbolType, String userId, String packet) {
        String destination = "/topic/tick"; // 사용자 구독 경로
        messagingTemplate.convertAndSendToUser(userId, destination, packet);
    }

    /**
     * 캔들 데이터를 전송
     */
    public void sendCandleDataToUser(SymbolType symbolType, String userId, String packet) {
        String destination = "/topic/candle"; // 사용자 구독 경로
        messagingTemplate.convertAndSendToUser(userId, destination, packet);
    }
}