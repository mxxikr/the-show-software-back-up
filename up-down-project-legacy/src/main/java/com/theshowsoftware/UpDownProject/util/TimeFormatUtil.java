package com.theshowsoftware.UpDownProject.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeFormatUtil {

    // yyyy-MM-dd HH:mm:ss.SSS 형식 포맷터
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 현재 시간을 yyyy-MM-dd HH:mm:ss.SSS 형식으로 반환
     */
    public static String formatCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }

    /**
     * 특정 LocalDateTime 객체를 yyyy-MM-dd HH:mm:ss.SSS 형식으로 변환
     */
    public static String format(LocalDateTime time) {
        return time.format(FORMATTER);
    }

    /**
     * 현재 시간의 타임스탬프 반환
     */
    public static long getCurrentTimestamp() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * yyyy-MM-dd HH:mm:ss.SSS 형식의 문자열을 LocalDateTime으로 변환
     */
    public static LocalDateTime parse(String time) {
        return LocalDateTime.parse(time, FORMATTER);
    }
}