package com.theshowsoftware.InternalTestPage.enums;

import lombok.Getter;

/**
 * 에러 범주 분류
 */
@Getter
public enum ErrorCategory {
    USER_INFO,
    SECURITY_CONFIG,
    CUSTOM_JSON_LOGIN
}