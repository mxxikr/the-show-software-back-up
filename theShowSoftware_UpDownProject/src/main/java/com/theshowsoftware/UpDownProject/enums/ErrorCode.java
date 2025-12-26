package com.theshowsoftware.UpDownProject.enums;


import lombok.Getter;

@Getter
public enum ErrorCode {
    // Round
    ROUND_NOT_ONGOING(10, "현재 진행 중인 라운드 정보가 없습니다."),
    ROUND_INVALID_INFORMATION(11, "해당 날짜와 라운드 번호에 대한 정보가 없습니다."),
    ROUND_NOT_FOUND(12, "해당 ID의 라운드를 찾을 수 없습니다."),
    ROUND_PRICE_NOT_FOUND(13, "해당 라운드의 가격 데이터를 찾을 수 없습니다."),
    ROUND_CREATION_FAILED(14, "라운드 생성에 실패했습니다."),
    DUPLICATE_ROUND(15, "이미 생성된 라운드입니다."),
    PRICE_FETCH_FAILED(16, "가격 정보를 가져오는 데 실패했습니다."),
    ROUND_NUMBER_CREATE_FAIL(17, "라운드 번호 생성 실패했습니다."),

    // RoundSetting
    INVALID_SETTING_REQUEST(18, "설정 정보가 올바르지 않습니다."),

    // BinancePrice
    API_NO_RESPONSE(19, "Binance API 응답이 없습니다."),
    API_ERROR(20, "Binance API 호출 중 오류가 발생했습니다."),
    API_PARSE_ERROR(21, "API 응답을 파싱하는 중 오류가 발생했습니다."),

    // Price
    JSON_SERIALIZATION_ERROR(22, "JSON 직렬화 오류");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}