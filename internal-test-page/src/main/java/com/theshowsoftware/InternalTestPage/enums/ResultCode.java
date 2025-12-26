package com.theshowsoftware.InternalTestPage.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS_HAS_DATA(1, "데이터 조회 성공"),
    SUCCESS_NO_DATA(0, "데이터 없음"),

    FAIL_DATA_ERROR(-1, "데이터 조회 실패/오류"),
    FAIL_INVALID_PARAMETER(-9, "입력 파라미터 오류"),
    INVAILD_SESSION_ERROR(-21, "세션 정보 오류"),
    FAIL_TOO_MANY_REQUEST(-91, "너무 많은 호출 시도"),

    ERROR_SERVER(-99, "서버 오류");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}