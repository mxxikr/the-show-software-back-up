package com.theshowsoftware.InternalTestPage.exception;

import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    /**
     * 기본 생성자
     * @param errorCode 에러 식별 코드
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드 외에 추가 메시지를 전달할 수 있는 생성자
     * @param errorCode 에러 식별 코드
     * @param customMessage 사용자 정의 상세 메시지
     */
    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드와 원인(throwable)을 함께 넘길 수 있는 생성자
     * @param errorCode 에러 식별 코드
     * @param cause 실제 예외 원인
     */
    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}