package com.theshowsoftware.InternalTestPage.repository;

import com.theshowsoftware.InternalTestPage.model.UserInfoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserInfoRepositoryImpl implements UserInfoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 회원 정보 저장 (INSERT/UPDATE 구분)
     * 사용자 이름 중복 시 예외 발생
     */
    @Override
    public UserInfoEntity save(UserInfoEntity userInfoEntity) {
        if (userInfoEntity.getUserId() == null) {
            // 신규 가입(INSERT)
            entityManager.persist(userInfoEntity);
            return userInfoEntity;
        } else {
            // 정보 수정(UPDATE)
            return entityManager.merge(userInfoEntity);
        }
    }

    /**
     * * 단일 회원 조회 (userId)
     */
    @Override
    public Optional<UserInfoEntity> findById(Long userId) {
        return Optional.ofNullable(entityManager.find(UserInfoEntity.class, userId));
    }

    /**
     * * 단일 회원 조회 (userName)
     */
    @Override
    public Optional<UserInfoEntity> findUserByName(String userName) {
        return findSingleResultByField("userName", userName);
    }

    /**
     * * 모든 회원 목록 조회
     */
    @Override
    public List<UserInfoEntity> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserInfoEntity> cq = cb.createQuery(UserInfoEntity.class);
        Root<UserInfoEntity> root = cq.from(UserInfoEntity.class);

        cq.select(root); // 전체 조회
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * 특정 필드 값을 가지고 단일 결과를 반환하는 공통 로직
     */
    private Optional<UserInfoEntity> findSingleResultByField(String fieldName, Object value) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserInfoEntity> cq = cb.createQuery(UserInfoEntity.class);
        Root<UserInfoEntity> root = cq.from(UserInfoEntity.class);

        cq.select(root).where(cb.equal(root.get(fieldName), value));

        try {
            return Optional.of(entityManager.createQuery(cq).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}