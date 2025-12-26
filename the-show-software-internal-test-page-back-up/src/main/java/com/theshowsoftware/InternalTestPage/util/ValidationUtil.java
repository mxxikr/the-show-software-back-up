package com.theshowsoftware.InternalTestPage.util;

import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.exception.CustomException;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 사용자 관련 검증 로직
 */
@Component
public class ValidationUtil {

    /**
     * 아이디, 비밀번호를 동시에 검증하는 메서드
     */
    public void validateCredentials(@NotNull String username, @NotNull String password) {
        if (!isValidUsername(username)) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_FORMAT);
        }
        if (!isValidPassword(password)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    /**
     * 아이디 검증
     * 영문 대소문자, 숫자, 특수문자(. _ -)만 허용
     * 최대 32바이트까지(UTF-8), 공백 불가
     */
    private boolean isValidUsername(String userName) {
        if (userName.getBytes(StandardCharsets.UTF_8).length > 32) {
            return false;
        }
        return userName.matches("^[A-Za-z0-9._-]+$");
    }

    /**
     * 비밀번호 검증
     * 영문 대소문자, 숫자, 특수문자(!@#$%^&*()-_)만 허용
     * 최소 6자 이상, 최대 64바이트(UTF-8), 공백 불가
     */
    private boolean isValidPassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 64) {
            return false;
        }
        if (password.length() < 6) {
            return false;
        }
        return password.matches("^[A-Za-z0-9!@#$%^&*()\\-_]+$");
    }
}