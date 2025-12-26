package com.theshowsoftware.UpDownProject.service;

import com.theshowsoftware.UpDownProject.dto.RoundResponseDTO;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RoundService {
    RoundResponseDTO getCurrentRound();

    RoundResponseDTO getRound(LocalDate roundDate, Integer roundNumber);

    @Transactional
    void finalizeRoundEndTime(Long roundId);

    @Transactional
    void updateRound(Long roundId, RoundStatus roundStatus, BigDecimal gameStartPrice, BigDecimal gameEndPrice);

    RoundResponseDTO getLatestFinishedRound();

    @Transactional
    RoundResponseDTO createNextRound();

    @Transactional
    void startNewDay();

    void deleteRoundsBefore(LocalDate cutoff);
}