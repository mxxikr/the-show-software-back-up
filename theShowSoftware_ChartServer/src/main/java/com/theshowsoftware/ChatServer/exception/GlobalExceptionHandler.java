package com.theshowsoftware.ChatServer.exception;

import com.theshowsoftware.ChatServer.dto.CommonResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponseDTO<String>> handleCustomException(CustomException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 클라이언트 오류 코드를 명시적으로 설정
                .body(CommonResponseDTO.failure(
                        ex.getErrorCode().getCode(),
                        ex.getErrorCode().getMessage()
                ));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseDTO<String>> handleOtherExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponseDTO.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "서버에서 알 수 없는 오류가 발생했습니다."
                ));
    }
}