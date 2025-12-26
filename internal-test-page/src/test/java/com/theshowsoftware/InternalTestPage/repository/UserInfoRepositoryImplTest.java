package com.theshowsoftware.InternalTestPage.repository;

import com.theshowsoftware.InternalTestPage.model.UserInfoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-dev.properties")
@Transactional // 테스트 실행 시 트랜잭션이 자동으로 관리되도록 설정
class UserInfoRepositoryImplTest {

    @Autowired
    private UserInfoRepository userInfoRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("새로운 사용자 저장 테스트")
    void testSaveNewUser() {
        // given
        UserInfoEntity newUser = new UserInfoEntity(
                "TestUser",
                "testPassword1234" // 비밀번호는 6자리 이상
        );

        // when
        UserInfoEntity saved = userInfoRepository.save(newUser);

        // then
        Assertions.assertNotNull(saved.getUserId(), "저장된 사용자 엔티티에는 ID가 있어야 합니다.");
        Assertions.assertEquals("TestUser", saved.getUserName());
    }

    @Test
    @DisplayName("ID로 사용자 조회 테스트")
    void testFindById() {
        // given
        UserInfoEntity user = new UserInfoEntity(
                "TestUser",
                "testPassword1234"
        );
        UserInfoEntity saved = userInfoRepository.save(user);

        // when
        Optional<UserInfoEntity> found = userInfoRepository.findById(saved.getUserId());

        // then
        Assertions.assertTrue(found.isPresent(), "저장된 사용자를 ID로 조회할 수 있어야 합니다.");
        Assertions.assertEquals("TestUser", found.get().getUserName());
    }

    @Test
    @DisplayName("특정 이름으로 사용자 조회 테스트")
    void testFindUserByName() {
        // given
        UserInfoEntity user = new UserInfoEntity("TestUser", "testPassword1234");
        userInfoRepository.save(user);

        // when
        Optional<UserInfoEntity> foundUser = userInfoRepository.findUserByName("TestUser");

        // then
        Assertions.assertTrue(foundUser.isPresent(), "TestUser으로 저장된 사용자를 조회할 수 있어야 합니다.");
        Assertions.assertEquals("TestUser", foundUser.get().getUserName(), "조회된 사용자의 이름이 일치해야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회 시 Optional.empty() 반환 테스트")
    void testFindUserByName_NotFound() {
        // given

        // when
        Optional<UserInfoEntity> foundUser = userInfoRepository.findUserByName("TestUser3");

        // then
        Assertions.assertTrue(foundUser.isEmpty(), "존재하지 않는 이름으로 조회할 경우 Optional.empty()가 반환되어야 합니다.");
    }

    @Test
    @DisplayName("동일한 이름으로 저장 불가능 확인 테스트")
    void testDuplicateUserNameConflict() {
        // given
        UserInfoEntity user1 = new UserInfoEntity("TestUser", "testPassword1234");
        UserInfoEntity user2 = new UserInfoEntity("TestUser", "testPassword1234");

        userInfoRepository.save(user1);

        // when & then
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            userInfoRepository.save(user2);
            entityManager.flush(); // flush를 통해 예외를 즉시 확인
        }, "동일한 이름으로 저장 시 DataIntegrityViolationException이 발생해야 합니다.");
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    void testFindAll() {
        // given
        UserInfoEntity user1 = new UserInfoEntity("TestUser", "testPassword1234");
        UserInfoEntity user2 = new UserInfoEntity("TestUser2", "testPassword1234");
        userInfoRepository.save(user1);
        userInfoRepository.save(user2);

        // when
        List<UserInfoEntity> allUsers = userInfoRepository.findAll();

        // then
        Assertions.assertTrue(allUsers.size() >= 2, "최소 2명 이상의 회원이 조회되어야 합니다.");
    }

    @Test
    @DisplayName("ID가 있는 엔티티를 업데이트(Merge) 하는 테스트")
    void testUpdateExistingUser() {
        // given
        UserInfoEntity userInfoEntity = new UserInfoEntity("TestUser", "testPassword1234");
        UserInfoEntity saved = userInfoRepository.save(userInfoEntity);

        // 새로운 이름으로 업데이트
        UserInfoEntity updatedEntity = new UserInfoEntity("TestUser2", "testPassword12345");
        updatedEntity.setUserId(saved.getUserId());

        // when
        UserInfoEntity merged = userInfoRepository.save(updatedEntity);

        // then
        Assertions.assertEquals(saved.getUserId(), merged.getUserId(), "ID는 동일해야 합니다.");
        Assertions.assertEquals("TestUser2", merged.getUserName(), "이름이 변경되어야 합니다.");
    }
}