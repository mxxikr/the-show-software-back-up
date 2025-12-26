package com.theshowsoftware.InternalTestPage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.enums.ResultCode;
import com.theshowsoftware.InternalTestPage.model.CommonResponseDTO;
import com.theshowsoftware.InternalTestPage.service.UserService;
import com.theshowsoftware.InternalTestPage.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String SIGN_UP_PATH = "/user_info/sign_up";
    private static final String LOGIN_PATH = "/user_info/login";
    private static final String LOGOUT_PATH = "/user_info/user_logout";
    private static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    private static final String SWAGGER_RESOURCES_PATH = "/swagger-resources/**";
    private static final String SWAGGER_UI_WEBJARS_PATH = "/webjars/**";
    private static final String SWAGGER_API_DOCS = "/v3/api-docs/**";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            UserServiceImpl userServiceImpl,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userServiceImpl)
                .passwordEncoder(passwordEncoder);
        return builder.build();
    }

    /**
     * SecurityFilterChain 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager, UserService userService
    ) throws Exception {
        http.addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class);

        // 기본 설정
        configureBasicSecurity(http);

        // 커스텀 JSON 로그인 필터
        http.addFilterBefore(
                new CustomJsonLoginFilter(authenticationManager, userService),
                UsernamePasswordAuthenticationFilter.class
        );
        // 권한 설정
        configureAuthorization(http);

        // 폼 로그인 설정
        configureFormLogin(http);

        // 로그아웃 설정
        configureLogout(http);

        // 인증/인가 예외 처리
        configureExceptionHandling(http);

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://192.168.0.11"));
        config.addAllowedHeader("Content-Type");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        config.setAllowCredentials(true); // 세션 인증 위해 필요
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * PasswordEncoder 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 기본 보안 설정
     */
    private void configureBasicSecurity(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // 세션 정책
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );
    }

    /**
     * 권한 설정
     */
    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(SIGN_UP_PATH, LOGIN_PATH,
                        SWAGGER_UI_PATH, SWAGGER_RESOURCES_PATH, SWAGGER_UI_WEBJARS_PATH, SWAGGER_API_DOCS).permitAll()
                .anyRequest().authenticated()
        );
    }

    /**
     * 폼 로그인 설정
     */
    private void configureFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginProcessingUrl(LOGIN_PATH)
                .successHandler((request, response, authentication) -> {
                    CommonResponseDTO<?> commonResponse = CommonResponseDTO.successHasData(
                            ErrorCode.LOGIN_SUCCESS.getMessage()
                    );
                    writeJsonResponse(response, HttpServletResponse.SC_OK, commonResponse);
                })
                .failureHandler((request, response, exception) -> {
                    CommonResponseDTO<?> commonResponse = CommonResponseDTO.failure(
                            ErrorCode.LOGIN_FAILURE.getCode(), ResultCode.INVAILD_SESSION_ERROR.getCode()
                    );
                    writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, commonResponse);
                })
                .permitAll()
        );
    }

    /**
     * 로그아웃 설정
     */
    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl(LOGOUT_PATH)
                .logoutSuccessHandler((request, response, authentication) -> {
                    CommonResponseDTO<?> commonResponse = CommonResponseDTO.successHasData(
                            ErrorCode.LOGOUT_SUCCESS.getMessage()
                    );
                    writeJsonResponse(response, HttpServletResponse.SC_OK, commonResponse);
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
        );
    }

    /**
     * 인증/인가 관련 예외 처리 설정
     */
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    // 인증되지 않은 상태로 보호 자원 접근 시
                    CommonResponseDTO<?> responseDto = CommonResponseDTO.failure(
                            ErrorCode.UNAUTHORIZED.getCode(), ResultCode.INVAILD_SESSION_ERROR.getCode()
                    );
                    writeJsonResponse(res, HttpServletResponse.SC_UNAUTHORIZED, responseDto);
                })
                .accessDeniedHandler((req, res, ex) -> {
                    // 권한 없이 자원 접근 시
                    CommonResponseDTO<?> responseDto = CommonResponseDTO.failure(
                            ErrorCode.ACCESS_DENIED.getCode(), ResultCode.INVAILD_SESSION_ERROR.getCode()
                    );
                    writeJsonResponse(res, HttpServletResponse.SC_FORBIDDEN, responseDto);
                })
        );
    }

    /**
     * JSON 응답 유틸리티
     */
    private void writeJsonResponse(HttpServletResponse response,
                                   int status,
                                   CommonResponseDTO<?> commonResponseDTO) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = objectMapper.writeValueAsString(commonResponseDTO);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}