package com.theshowsoftware.InternalTestPage.service;


import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.exception.CustomException;
import com.theshowsoftware.InternalTestPage.model.UserInfoRequestDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoEntity;
import com.theshowsoftware.InternalTestPage.model.UserInfoResponseDTO;
import com.theshowsoftware.InternalTestPage.repository.UserInfoRepository;
import com.theshowsoftware.InternalTestPage.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // 디폴트 읽기 전용
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationUtil validationUtil;

    /**
     * 회원 가입
     */
    @Override
    @Transactional
    public UserInfoResponseDTO signUp(UserInfoRequestDTO userInfoRequestDTO) {
        // 아이디/비밀번호 형식 검증
        validationUtil.validateCredentials(userInfoRequestDTO.getUserName(), userInfoRequestDTO.getPassword());

        // 중복 회원 검증
        validateDuplicateMember(userInfoRequestDTO.getUserName());

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(userInfoRequestDTO.getPassword());

        // 신규 사용자 생성
        UserInfoEntity userEntity = new UserInfoEntity(
                userInfoRequestDTO.getUserName(),
                hashedPassword
        );

        // DB 저장
        userInfoRepository.save(userEntity);

        UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
        userInfoResponseDTO.setUserId(userEntity.getUserId());
        userInfoResponseDTO.setUserName(userEntity.getUserName());

        return userInfoResponseDTO;
    }

    /**
     * 전체 회원 조회
     */
    @Override
    public List<UserInfoResponseDTO> findMembers() {
        return userInfoRepository.findAll()
                .stream()
                .map(entity -> new UserInfoResponseDTO(
                        entity.getUserId(),
                        entity.getUserName(),
                        entity.getUserAmount(),
                        entity.getCreatedDate(),
                        entity.getUpdatedDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 회원 조회 (userId)
     */
    @Override
    public Optional<UserInfoResponseDTO> findById(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_INFORMATION);
        }

        UserInfoEntity entity = userInfoRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return Optional.of(new UserInfoResponseDTO(
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserAmount(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        ));
    }

    /**
     * 회원 조회 (userName)
     */
    @Override
    public Optional<UserInfoResponseDTO> findUserByName(String userName) {
        if (userName == null) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_FORMAT);
        }

        UserInfoEntity entity = userInfoRepository.findUserByName(userName)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return Optional.of(new UserInfoResponseDTO(
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserAmount(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        ));
    }

    /**
     * 로그인
     */
    @Override
    public Long login(String userName, String password) {
        // 아이디/비밀번호 형식 검증
        validationUtil.validateCredentials(userName, password);

        // 사용자 조회
        UserInfoEntity entity = userInfoRepository.findUserByName(userName)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 평문 비밀번호와 해싱 비밀번호 비교
        if (!passwordEncoder.matches(password, entity.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        return entity.getUserId();
    }

    /**
     * 로그 아웃
     */
    @Override
    public void logout(HttpSession session) {
        // 세션 무효화
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * UserDetailsService 구현 메서드
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws CustomException {
        // DB에서 사용자 정보 조회
        UserInfoEntity userEntity = userInfoRepository.findUserByName(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 스프링 시큐리티에서 제공하는 User 객체로 반환
        return new org.springframework.security.core.userdetails.User(
                userEntity.getUserName(),
                userEntity.getPassword(),
                Collections.emptyList()
        );
    }

    /**
     * 중복 회원 검증
     */
    private void validateDuplicateMember(String userName) {
        // 이미 존재하는 사용자라면 예외 발생
        userInfoRepository.findUserByName(userName).ifPresent(u -> {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        });
    }
}