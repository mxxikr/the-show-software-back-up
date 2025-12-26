package com.theshowsoftware.ChatServer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponseDTO<T> {

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonProperty("message")
    private String message;

    public static <T> CommonResponseDTO<T> successHasData(T data, String message) {
        return new CommonResponseDTO<>(1, data, message);
    }

    public static <T> CommonResponseDTO<T> successNoData(String message) {
        return new CommonResponseDTO<>(0, null, message);
    }

    public static <T> CommonResponseDTO<T> failure(int resultCode, String message) {
        return new CommonResponseDTO<>(resultCode, null, message);
    }
}