package com.theshowsoftware.UpDownProject.controller;

import com.theshowsoftware.UpDownProject.enums.ResultCode;
import com.theshowsoftware.UpDownProject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.theshowsoftware.UpDownProject.dto.RoundResponseDTO;
import com.theshowsoftware.UpDownProject.dto.CommonResponseDTO;
import com.theshowsoftware.UpDownProject.service.RoundService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/game/updown")
@RequiredArgsConstructor
public class RoundController {
    private final RoundService roundService;

    /**
     * 현재 진행중인 라운드 정보 반환
     */
    @GetMapping("/get_current_round")
    public CommonResponseDTO<RoundResponseDTO> getCurrentRound() {
        try {
            RoundResponseDTO response = roundService.getCurrentRound();
            return new CommonResponseDTO<>(
                    ResultCode.SUCCESS_HAS_DATA.getCode(),
                    response,
                    ResultCode.SUCCESS_HAS_DATA.getMessage()
            );
        } catch (IllegalStateException e) {
            return new CommonResponseDTO<>(
                    ResultCode.SUCCESS_NO_DATA.getCode(),
                    null,
                    ResultCode.SUCCESS_NO_DATA.getMessage()
            );
        } catch (Exception e) {
            return new CommonResponseDTO<>(
                    ResultCode.ERROR_SERVER.getCode(),
                    null,
                    ResultCode.ERROR_SERVER.getMessage()
            );
        }
    }

    /**
     * 특정 날짜의 특정 라운드 정보 반환
     */
    @GetMapping("/get_round/{roundDate}/{roundNum}")
    public CommonResponseDTO<RoundResponseDTO> getRound( @PathVariable("roundDate") String roundDate,
                                                         @PathVariable("roundNum") Integer roundNum) {
        try {
            LocalDate parsedDate = LocalDate.parse(roundDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            RoundResponseDTO roundResponseDTO = roundService.getRound(parsedDate, roundNum);
            return new CommonResponseDTO<>(
                    ResultCode.SUCCESS_HAS_DATA.getCode(),
                    roundResponseDTO,
                    ResultCode.SUCCESS_HAS_DATA.getMessage()
            );
        } catch (CustomException e) {
            return new CommonResponseDTO<>(
                    ResultCode.FAIL_INVALID_PARAMETER.getCode(),
                    null,
                    ResultCode.FAIL_INVALID_PARAMETER.getMessage()
            );
        } catch (IllegalArgumentException e) {
            return new CommonResponseDTO<>(
                    ResultCode.FAIL_INVALID_PARAMETER.getCode(),
                    null,
                    ResultCode.FAIL_INVALID_PARAMETER.getMessage()
            );
        } catch (Exception e) {
            return new CommonResponseDTO<>(
                    ResultCode.ERROR_SERVER.getCode(),
                    null,
                    ResultCode.ERROR_SERVER.getMessage()
            );
        }
    }

    /**
     * round_status가 FINISHED인 가장 최신 데이터를 반환
     */
    @GetMapping("/get_latest_finished")
    public CommonResponseDTO<RoundResponseDTO> getLatestFinishedRound() {
        try {
            RoundResponseDTO response = roundService.getLatestFinishedRound();
            return new CommonResponseDTO<>(
                    ResultCode.SUCCESS_HAS_DATA.getCode(),
                    response,
                    ResultCode.SUCCESS_HAS_DATA.getMessage()
            );
        } catch (CustomException e) {
            return new CommonResponseDTO<>(
                    ResultCode.SUCCESS_NO_DATA.getCode(),
                    null,
                    ResultCode.SUCCESS_NO_DATA.getMessage()
            );
        } catch (Exception e) {
            return new CommonResponseDTO<>(
                    ResultCode.ERROR_SERVER.getCode(),
                    null,
                    ResultCode.ERROR_SERVER.getMessage()
            );
        }
    }
}