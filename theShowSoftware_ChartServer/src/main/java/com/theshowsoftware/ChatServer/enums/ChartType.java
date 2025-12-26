package com.theshowsoftware.ChatServer.enums;

import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
public enum ChartType {
    TICK("0x00", "TICK", 1, ChronoUnit.SECONDS), // 틱 데이터(1초 단위)
    ONE_SECOND("0x01", "1s", 1, ChronoUnit.SECONDS), // 1초 봉
    THREE_SECONDS("0x02", "3s", 3, ChronoUnit.SECONDS), // 3초 봉
    FIVE_SECONDS("0x03", "5s", 5, ChronoUnit.SECONDS), // 5초 봉
    TEN_SECONDS("0x04", "10s", 10, ChronoUnit.SECONDS), // 10초 봉
    THIRTY_SECONDS("0x05", "30s", 30, ChronoUnit.SECONDS), // 30초 봉
    ONE_MINUTE("0x06", "1m", 1, ChronoUnit.MINUTES), // 1분 봉
    THREE_MINUTES("0x07", "3m", 3, ChronoUnit.MINUTES), // 3분 봉
    FIVE_MINUTES("0x08", "5m", 5, ChronoUnit.MINUTES), // 5분 봉
    TEN_MINUTES("0x09", "10m", 10, ChronoUnit.MINUTES), // 10분 봉
    FIFTEEN_MINUTES("0x0A", "15m", 15, ChronoUnit.MINUTES), // 15분 봉
    ONE_HOUR("0x0B", "1h", 1, ChronoUnit.HOURS), // 1시간 봉
    THREE_HOURS("0x0C", "3h", 3, ChronoUnit.HOURS), // 3시간 봉
    FIVE_HOURS("0x0D", "5h", 5, ChronoUnit.HOURS), // 5시간 봉
    TWELVE_HOURS("0x0E", "12h", 12, ChronoUnit.HOURS), // 12시간 봉
    ONE_DAY("0x0F", "1d", 1, ChronoUnit.DAYS), // 1일 봉
    THREE_DAYS("0x10", "3d", 3, ChronoUnit.DAYS), // 3일 봉
    ONE_WEEK("0x11", "1w", 7, ChronoUnit.DAYS), // 1주 봉
    ONE_MONTH("0x12", "1M", 1, ChronoUnit.MONTHS); // 1달 봉

    private final String hexCode;
    private final String label; // 캔들 종류
    private final long amount; // 단위 시간 크기
    private final ChronoUnit unit; // 시간 단위
    private final Duration duration;

    ChartType(String hexCode, String label, long amount, ChronoUnit unit) {
        this.hexCode = hexCode;
        this.label = label;
        this.amount = amount;
        this.unit = unit;
        if (unit != null && unit.isTimeBased()) {
            this.duration = Duration.of(amount, unit);
        } else {
            this.duration = null;
        }
    }

    /**
     * 캔들 간격을 밀리초 단위로 반환
     */
    public long getIntervalInMillis() {
        return duration.toMillis();
    }

    /**
     * 주어진 시간을 캔들 간격으로 자름
     */
    public static Instant truncateTime(Instant time, ChartType chartType) {
        long epochSeconds = time.getEpochSecond();
        long intervalSeconds = chartType.getDuration().getSeconds();
        long truncatedSeconds = (epochSeconds / intervalSeconds) * intervalSeconds;
        return Instant.ofEpochSecond(truncatedSeconds);
    }

    /**
     * 주어진 시간의 다음 캔들 시간을 계산
     */
    public Instant getNext(Instant time) {
        return time.plus(duration);
    }
}