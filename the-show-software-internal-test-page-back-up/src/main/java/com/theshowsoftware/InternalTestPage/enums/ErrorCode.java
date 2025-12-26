package com.theshowsoftware.InternalTestPage.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // USER_INFO
    INVALID_USERNAME_FORMAT(ErrorCategory.USER_INFO, "INVALID_USERNAME_FORMAT",
            "아이디 형식이 잘못되었습니다. 영문/숫자/._-만 허용하며, 최대 32바이트입니다."),
    DUPLICATE_USERNAME(ErrorCategory.USER_INFO, "DUPLICATE_USERNAME",
            "이미 존재하는 회원입니다."),
    INVALID_PASSWORD_FORMAT(ErrorCategory.USER_INFO, "INVALID_PASSWORD_FORMAT",
            "비밀번호 형식이 잘못되었습니다. 영문/숫자 및 특정 특수문자만 허용하며, 6자 이상 64바이트 이하입니다."),
    PASSWORD_MISMATCH(ErrorCategory.USER_INFO, "PASSWORD_MISMATCH",
            "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(ErrorCategory.USER_INFO, "USER_NOT_FOUND",
            "존재하지 않는 회원입니다."),

    // SECURITY_CONFIG
    LOGIN_SUCCESS(ErrorCategory.SECURITY_CONFIG, "LOGIN_SUCCESS", "로그인 성공"),
    LOGIN_FAILURE(ErrorCategory.SECURITY_CONFIG, "LOGIN_FAILURE", "로그인 실패"),
    LOGOUT_SUCCESS(ErrorCategory.SECURITY_CONFIG, "LOGOUT_SUCCESS", "로그아웃 성공"),
    LOGOUT_FAILURE(ErrorCategory.SECURITY_CONFIG, "LOGOUT_FAILURE", "로그아웃 실패"),
    AUTH_REQUIRED(ErrorCategory.SECURITY_CONFIG, "AUTH_REQUIRED", "인증이 필요합니다."),
    INVALID_INFORMATION(ErrorCategory.SECURITY_CONFIG, "INVALID_INFORMATION", "유효하지 않은 로그인 정보입니다."),
    ACCESS_DENIED(ErrorCategory.SECURITY_CONFIG, "ACCESS_DENIED", "접근 권한이 없습니다."),
    UNAUTHORIZED(ErrorCategory.SECURITY_CONFIG, "UNAUTHORIZED", "인증되지 않은 요청입니다."),

    // CUSTOM_JSON_LOGIN 범주
    NOT_JSON_TYPE(ErrorCategory.CUSTOM_JSON_LOGIN, "NOT_JSON_TYPE", "JSON 타입이 아닙니다."),
    JSON_LOGIN_SUCCESS(ErrorCategory.CUSTOM_JSON_LOGIN, "JSON_LOGIN_SUCCESS", "JSON 로그인 성공"),
    JSON_LOGIN_FAILURE(ErrorCategory.CUSTOM_JSON_LOGIN, "JSON_LOGIN_FAILURE", "JSON 로그인 실패");

    /**
     * 에러 범주
     */
    private final ErrorCategory category;

    /**
     * 에러 식별 코드
     */
    private final String code;
    /**
     * 에러 메시지
     */
    private final String message;

    ErrorCode(ErrorCategory category, String code, String message) {
        this.category = category;
        this.code = code;
        this.message = message;
    }

    /**
     * 코드와 메시지를 결합해 하나의 문자열로 반환
     */
    public String getFullMessage() {
        return String.format("[%s/%s] %s", category.name(), code, message);
    }
}