package com.theshowsoftware.InternalTestPage.service;

import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.exception.CustomException;
import com.theshowsoftware.InternalTestPage.model.UserInfoRequestDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoResponseDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-dev.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("중복 회원 없는 경우: 회원 가입 성공")
    void join_NoDuplicate_Success() {
        // given
        UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO("TestUser", null, "testPassword1234");

        // when
        UserInfoResponseDTO userInfoResponseDTO = userService.signUp(userInfoRequestDTO);

        // then
        // ID가 null이 아니어야 회원 가입 성공
        assertNotNull(userInfoResponseDTO);

        // DB에서 실제로 id=userId인 엔티티가 존재하는지 검증
        Optional<UserInfoResponseDTO> resultUserInfoResponseDTO = userService.findById(userInfoResponseDTO.getUserId());
        assertTrue(resultUserInfoResponseDTO.isPresent());
        assertEquals("TestUser", resultUserInfoResponseDTO.get().getUserName());
    }

    @Test
    @DisplayName("중복 회원인 경우: 예외 발생")
    void join_Duplicate_ThrowsException() {
        // given
        UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO("TestUser", null, "testPassword1234");
        userService.signUp(userInfoRequestDTO);
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.signUp(userInfoRequestDTO));

        // when & then
        assertEquals(ErrorCode.DUPLICATE_USERNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("전체 회원 조회")
    void findAllMembers() {
        // given
        UserInfoRequestDTO user1 = new UserInfoRequestDTO("TestUser1", null, "testPassword1234");
        UserInfoRequestDTO user2 = new UserInfoRequestDTO("TestUser2", null, "testPassword1234");
        userService.signUp(user1);
        userService.signUp(user2);


        // when
        List<UserInfoResponseDTO> allMembers = userService.findMembers();

        // then
        assertFalse(allMembers.isEmpty());
        assertTrue(allMembers.size() >= 2);
    }

    @Test
    @DisplayName("특정 ID로 회원 조회")
    void findOneMember() {
        // given
        UserInfoResponseDTO savedDto = userService.signUp(new UserInfoRequestDTO("TestUser", null,"testPassword1234"));
        Long savedId = savedDto.getUserId();

        // when
        Optional<UserInfoResponseDTO> foundUser = userService.findById(savedId);

        // then
        assertTrue(foundUser.isPresent());
        assertEquals("TestUser", foundUser.get().getUserName());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원 조회 시 Optional.empty()")
    void findOneMember_NotFound() {
        // given
        Long nonExistsId = 99999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.findById(nonExistsId));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 성공 - 정상 아이디, 비밀번호")
    void login_Success() {
        // given
        UserInfoResponseDTO savedDto = userService.signUp(new UserInfoRequestDTO("TestUser", null, "testPassword1234"));
        Long savedId = savedDto.getUserId();

        // when
        Long loggedInId = userService.login("TestUser", "testPassword1234");

        // then
        assertNotNull(loggedInId);
        assertEquals(savedId, loggedInId);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_Fail_UserNotFound() {
        // given

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.login("TestUser", "testPassword1234")
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_PasswordMismatch() {
        // given
        userService.signUp(new UserInfoRequestDTO("TestUser", null, "testPassword1234"));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.login("TestUser", "testPassword123456")
        );
        assertEquals(ErrorCode.PASSWORD_MISMATCH, exception.getErrorCode());
    }
}