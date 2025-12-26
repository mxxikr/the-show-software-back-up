package com.theshowsoftware.UpDownProject.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RoundBroadCastService {
    void sendRoundInfo(String sessionId);

    void cacheRoundProcessingTime(Long roundId, LocalDateTime gameStartTime, LocalDateTime gameEndTime);

    void registerSessionForStartPrice(String sessionId);

    void registerSessionForEndPrice(String sessionId);

    void sendRoundProcessingStartPrice(Long roundId, BigDecimal gameStartPrice);

    void sendRoundProcessingEndPrice(Long roundId, BigDecimal gameEndPrice);
}