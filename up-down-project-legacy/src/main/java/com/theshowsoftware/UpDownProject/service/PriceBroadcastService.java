package com.theshowsoftware.UpDownProject.service;

import java.math.BigDecimal;

public interface PriceBroadcastService {
    void startBroadcast(String sessionId);

    void stopBroadcast(String sessionId);

    void sendHistoryPrices(String sessionId);

    void requestLivePrices(String sessionId);

    BigDecimal getCachedLatestPrice();
}