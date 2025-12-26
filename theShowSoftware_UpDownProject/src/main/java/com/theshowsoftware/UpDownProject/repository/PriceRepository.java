package com.theshowsoftware.UpDownProject.repository;

import com.theshowsoftware.UpDownProject.domain.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * 지정된 날짜 이전의 데이터 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByTimestampBefore(LocalDateTime cutoffDate);
}