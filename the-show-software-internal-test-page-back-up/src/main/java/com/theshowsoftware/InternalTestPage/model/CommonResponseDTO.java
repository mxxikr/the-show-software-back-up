package com.theshowsoftware.InternalTestPage.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"result_code"})
public class CommonResponseDTO<T> {

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 데이터가 존재하는 성공 응답
     * result_code = 1
     */
    public static <T> CommonResponseDTO<T> successHasData(T data) {
        return new CommonResponseDTO<>(1, data, null);
    }

    /**
     * 데이터가 없지만, 요청 자체는 성공한 응답
     * result_code = 0
     */
    public static <T> CommonResponseDTO<T> successNoData() {
        return new CommonResponseDTO<>(0, null, null);
    }

    /**
     * 오류
     */
    public static <T> CommonResponseDTO<T> failure(String errorMessage, int resultCode) {
        return new CommonResponseDTO<>(resultCode, null, errorMessage);
    }
}