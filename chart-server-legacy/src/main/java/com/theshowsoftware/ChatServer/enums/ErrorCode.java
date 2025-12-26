package com.theshowsoftware.ChatServer.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // ChartCache
    CACHE_SYMBOL_NOT_FOUND(100, "심볼 데이터가 캐시에 존재하지 않습니다."),
    CACHE_INVALID_TIME_RANGE(101, "시간 범위가 잘못되었습니다."),
    CACHE_DATA_IS_NULL(102, "데이터가 Null 값 입니다.."),
    CACHE_ADD_CANDLE_ERROR(103, "Candle 데이터를 추가하는 중 오류가 발생했습니다."),
    CACHE_FLUSH_ERROR(104, "Tick 데이터를 비우는 중 오류가 발생했습니다."),
    CACHE_RETRIEVE_ERROR(105, "캐시 데이터 조회 중 오류가 발생했습니다."),
    INVALID_TICK_PRICE(106, "틱 가격이 유효하지 않습니다."),

    // ChartScheduler
    SCHEDULER_TICK_ERROR(200, "Tick 데이터를 생성하는 중 오류가 발생했습니다."),
    SCHEDULER_CANDLE_ERROR(201, "Candle 데이터를 생성하는 중 오류가 발생했습니다."),
    SCHEDULER_START_ERROR(202, "스케줄러를 시작하는 중 오류가 발생했습니다."),
    SCHEDULER_SHUTDOWN_ERROR(203, "스케줄러를 종료하는 중 오류가 발생했습니다."),

    // PacketManager
    INVAILD_PACKET_STRUCTURE(249, "유효하지 않은 패킷 구조체입니다."),
    UNKOWUN_CHART_TYPE(250, "알 수 없는 차트 타입입니다."),
    INVALID_TICK_PACKET_FORMAT(251, "올바르지 않은 틱 패킷 형식입니다."),
    INVALID_CANDLE_PACKET_FORMAT(252, "올바르지 않은 캔들 패킷 형식입니다."),
    FAILD_CANDLE_PARSE_ERROR(253, "캔들 패킷 디패키징에 실패했습니다."),
    FAILED_TICK_PARSE_ERROR(254, "틱 패킷 패키징에 실패했습니다."),
    INVALID_PACKET_FORMAT(255, "패킷 형식이 올바르지 않습니다."),
    PACKET_INVALID_PARTS_COUNT(256, "패킷의 파트 개수가 올바르지 않습니다."),
    PACKET_BASE_SYMBOL_INVALID(257, "패킷의 기준 심볼이 올바르지 않습니다.");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}