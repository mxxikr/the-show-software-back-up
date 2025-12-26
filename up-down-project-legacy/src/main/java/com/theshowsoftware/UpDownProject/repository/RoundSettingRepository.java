package com.theshowsoftware.UpDownProject.repository;

import com.theshowsoftware.UpDownProject.domain.RoundSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoundSettingRepository extends JpaRepository<RoundSettingEntity, Long> {
    /**
     * 최신 라운드 설정값 조회
     */
    Optional<RoundSettingEntity> findTopByOrderByIdDesc();
}