package com.theshowsoftware.InternalTestPage.repository;

import com.theshowsoftware.InternalTestPage.model.UserInfoEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 회원 저장소 인터페이스
 */
@Repository
public interface UserInfoRepository {
    // 회원 정보 저장
    UserInfoEntity save(UserInfoEntity userInfoEntity);
    
    // 회원 ID로 조회
    Optional<UserInfoEntity> findById(Long userId);
    
    // 회원 이름으로 조회
    Optional<UserInfoEntity> findUserByName(String userName);

    // 전체 회원 목록 반환
    List<UserInfoEntity> findAll();
}