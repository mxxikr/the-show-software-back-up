package com.theshowsoftware.InternalTestPage.service;

import com.theshowsoftware.InternalTestPage.model.UserInfoRequestDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserInfoResponseDTO signUp(UserInfoRequestDTO requestDTO);

    List<UserInfoResponseDTO> findMembers();

    Optional<UserInfoResponseDTO> findById(Long userId);

    Optional<UserInfoResponseDTO> findUserByName(String userName);

    Long login(String userName, String password);

    void logout(HttpSession session);

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}