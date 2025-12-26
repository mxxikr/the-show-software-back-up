package com.theshowsoftware.UpDownProject.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PriceService {
    BigDecimal fetchCurrentPrice(boolean isRoundSchedulerCall);

    String getCurrentPrices(BigDecimal latestPrice);

    Map<String, Object> getHistoryPrices();
}