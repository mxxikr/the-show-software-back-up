package com.theshowsoftware.UpDownProject.repository;

import com.theshowsoftware.UpDownProject.domain.RoundEntity;
import com.theshowsoftware.UpDownProject.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {
    /**
     * 가장 최신 라운드 조회
     * - ID 기준으로 가장 최신 데이터를 반환
     */
    Optional<RoundEntity> findTopByOrderByIdDesc();

    /**
     * FINISHED 된 라운드에서 가장 최신 라운드 데이터를 조회
     */
    Optional<RoundEntity> findTopByRoundStatusOrderByIdDesc(RoundStatus roundStatus);

    /**
     * 특정 날짜와 라운드 번호로 라운드 조회
     */
    Optional<RoundEntity> findByRoundDateAndRoundNum(LocalDate roundDate, Integer roundNum);

    /**
     * 특정 날짜 기준 특정 상태의 라운드 목록 조회
     */
    List<RoundEntity> findAllByRoundDateAndRoundStatus(LocalDate roundDate, RoundStatus roundStatus);

    /**
     * 특정 날짜 기준 FINISHED 상태가 아닌 라운드 조회
     */
    List<RoundEntity> findAllByRoundDateAndRoundStatusNot(LocalDate roundDate, RoundStatus roundStatus);

    /**
     * 특정 날짜 이전의 라운드 데이터를 조회
     */
    List<RoundEntity> findAllByRoundDateBefore(LocalDate roundDate);

    /**
     * 특정 날짜 이전의 모든 라운드 데이터를 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByRoundDateBefore(LocalDate roundDate);

    /**
     * 특정 날짜 이전의 라운드 개수를 조회
     */
    long countByRoundDateBefore(LocalDate roundDate);

    /**
     * 특정 날짜의 라운드 번호를 기준으로 다음 라운드 번호를 조회
     */
    @Query("SELECT COALESCE(MAX(r.roundNum), 0) + 1 FROM RoundEntity r WHERE r.roundDate = :roundDate")
    Optional<Integer> getNextRoundNumber(@Param("roundDate") LocalDate roundDate);

    /**
     * 특정 날짜와 라운드 번호로 라운드 존재 여부 조회
     */
    boolean existsByRoundDateAndRoundNum(LocalDate roundDate, Integer roundNum);

}