package com.theshowsoftware.InternalTestPage.config;

import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.enums.ResultCode;
import com.theshowsoftware.InternalTestPage.exception.CustomException;
import com.theshowsoftware.InternalTestPage.model.CommonResponseDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoResponseDTO;
import com.theshowsoftware.InternalTestPage.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

class CustomJsonLoginFilter extends AbstractAuthenticationProcessingFilter {
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // JSON 인증 요청 경로
    private static final String LOGIN_REQUEST_PATH = "/user_info/login";
    // 인증 요청 메서드
    private static final String HTTP_METHOD_POST = "POST";

    public CustomJsonLoginFilter(AuthenticationManager authenticationManager,
                                 UserService userService) {
        super(new AntPathRequestMatcher(LOGIN_REQUEST_PATH, HTTP_METHOD_POST));
        this.userService = userService;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        if (!isJsonRequest(request)) {
            throw new CustomException(ErrorCode.NOT_JSON_TYPE);
        }

        Map<String, String> credentials = objectMapper.readValue(
                request.getInputStream(),
                new TypeReference<Map<String, String>>() {}
        );
        String username = credentials.get("userName");
        String password = credentials.get("password");

        try {
            userService.login(username, password);
        } catch (CustomException e) {
            // AuthenticationException 계열로 변환하여 throw
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);

        return getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        SecurityContext securityContext = buildSecurityContext(authResult);
        saveSecurityContextToSession(securityContext, request, response);

        org.springframework.security.core.userdetails.User userPrincipal =
                (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String username = userPrincipal.getUsername();

        Optional<UserInfoResponseDTO> userInfoOpt = userService.findUserByName(username);
        UserInfoResponseDTO userInfo = userInfoOpt.orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userId", userInfo.getUserId());
        dataMap.put("userName", userInfo.getUserName());

        CommonResponseDTO<?> commonResponseDTO = CommonResponseDTO.successHasData(dataMap);

        String jsonResponse = objectMapper.writeValueAsString(commonResponseDTO);
        writeJsonResponse(response, HttpServletResponse.SC_OK, jsonResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {

        Throwable cause = failed.getCause();
        if (cause instanceof CustomException customEx) {

            CommonResponseDTO<?> commonResponseDTO = CommonResponseDTO.failure(
                    customEx.getErrorCode().getCode(),
                    ResultCode.FAIL_INVALID_PARAMETER.getCode()
            );
            String jsonResponse = objectMapper.writeValueAsString(commonResponseDTO);
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, jsonResponse);
        } else {
            // 일반 AuthenticationException 처리
            CommonResponseDTO<?> commonResponseDTO = CommonResponseDTO.failure(
                    ErrorCode.UNAUTHORIZED.getCode(),
                    ResultCode.FAIL_INVALID_PARAMETER.getCode()
            );
            String jsonResponse = objectMapper.writeValueAsString(commonResponseDTO);
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, jsonResponse);
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private SecurityContext buildSecurityContext(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return context;
    }

    private void saveSecurityContextToSession(SecurityContext context,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);
    }

    private void writeJsonResponse(HttpServletResponse response, int status, String jsonContent) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=" + StandardCharsets.UTF_8);
        response.getWriter().write(jsonContent);
    }
}