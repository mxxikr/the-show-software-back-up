package com.theshowsoftware.InternalTestPage.controller;

import com.theshowsoftware.InternalTestPage.enums.ErrorCode;
import com.theshowsoftware.InternalTestPage.enums.ResultCode;
import com.theshowsoftware.InternalTestPage.exception.CustomException;
import com.theshowsoftware.InternalTestPage.model.CommonResponseDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoRequestDTO;
import com.theshowsoftware.InternalTestPage.model.UserInfoResponseDTO;
import com.theshowsoftware.InternalTestPage.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@ResponseBody
@RequestMapping("/user_info")
@Controller
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 회원 가입
     */
    @PostMapping("/sign_up")
    public ResponseEntity<CommonResponseDTO<UserInfoResponseDTO>> signUp(@RequestBody UserInfoRequestDTO userInfoRequestDTO) {
        try {
            UserInfoResponseDTO userInfoResponseDTO = userService.signUp(userInfoRequestDTO);
            if (userInfoResponseDTO != null) {
                return ResponseEntity.ok(
                        CommonResponseDTO.successHasData(userInfoResponseDTO)
                );
            } else {
                return ResponseEntity.ok(
                        CommonResponseDTO.successNoData()
                );
            }
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(ResultCode.ERROR_SERVER.getMessage(), ResultCode.ERROR_SERVER.getCode()));
        }
    }

    /**
     * 전체 회원 조회
     */
    @GetMapping("/find_user_all")
    public ResponseEntity<CommonResponseDTO<List<UserInfoResponseDTO>>> findUserAll() {
        try {
            List<UserInfoResponseDTO> members = userService.findMembers();
            if (members == null || members.isEmpty()) {
                return ResponseEntity.ok(
                        CommonResponseDTO.successNoData()
                );
            } else {
                return ResponseEntity.ok(
                        CommonResponseDTO.successHasData(members)
                );
            }
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(ResultCode.ERROR_SERVER.getMessage(), ResultCode.ERROR_SERVER.getCode()));
        }
    }

    /**
     * 특정 ID로 회원 조회
     */
    @GetMapping("/find_user_by_id")
    public ResponseEntity<CommonResponseDTO<UserInfoResponseDTO>> findUserById(@RequestBody UserInfoRequestDTO userInfoRequestDTO) {
        try {
            Optional<UserInfoResponseDTO> userOpt = userService.findById(userInfoRequestDTO.getUserId());
            return userOpt
                    .map(userInfoResponseDTO -> ResponseEntity.ok(
                            CommonResponseDTO.successHasData(userInfoResponseDTO)
                    ))
                    .orElseGet(() -> ResponseEntity.ok(
                            CommonResponseDTO.successNoData()
                    ));
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(
                            ResultCode.ERROR_SERVER.getMessage(),
                            ResultCode.ERROR_SERVER.getCode()
                    ));
        }
    }

    /**
     * 특정 userName으로 회원 조회
     */
    @GetMapping("/find_user_by_name")
    public ResponseEntity<CommonResponseDTO<UserInfoResponseDTO>> findUserByName(@RequestBody UserInfoRequestDTO userInfoRequestDTO) {
        try {
            Optional<UserInfoResponseDTO> userOpt = userService.findUserByName(userInfoRequestDTO.getUserName());
            return userOpt
                    .map(userInfoResponseDTO -> ResponseEntity.ok(
                            CommonResponseDTO.successHasData(userInfoResponseDTO)
                    ))
                    .orElseGet(() -> ResponseEntity.ok(
                            CommonResponseDTO.successNoData()
                    ));
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(
                            ResultCode.ERROR_SERVER.getMessage(),
                            ResultCode.ERROR_SERVER.getCode()
                    ));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<CommonResponseDTO<Long>> login(@RequestBody UserInfoRequestDTO userInfoRequestDTO,
                                                         HttpSession session) {
        try {
            Long userId = userService.login(
                    userInfoRequestDTO.getUserName(),
                    userInfoRequestDTO.getPassword()
            );

            if (userId != null) {
                UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                        userInfoRequestDTO.getUserName(),
                        userInfoRequestDTO.getPassword()
                );
                Authentication authResult = authenticationManager.authenticate(authReq);
                SecurityContextHolder.getContext().setAuthentication(authResult);
                session.setAttribute("USER_ID", userId);

                return ResponseEntity.ok(
                        CommonResponseDTO.successHasData(userId)
                );
            } else {
                return ResponseEntity.ok(
                        CommonResponseDTO.successNoData()
                );
            }
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(ResultCode.ERROR_SERVER.getMessage(), ResultCode.ERROR_SERVER.getCode()));
        }
    }

    /**
     * 로그인한 사용자 정보 조회
     */
    @GetMapping("/my_info")
    public ResponseEntity<CommonResponseDTO<UserInfoResponseDTO>> myInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok(
                        CommonResponseDTO.successNoData()
                );
            }

            String userName = authentication.getName();
            Optional<UserInfoResponseDTO> userInfoOpt = userService.findUserByName(userName);
            return userInfoOpt.map(userInfoResponseDTO -> ResponseEntity.ok(
                    CommonResponseDTO.successHasData(userInfoResponseDTO)
            )).orElseGet(() -> ResponseEntity.ok(
                    CommonResponseDTO.successNoData()
            ));
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(ResultCode.ERROR_SERVER.getMessage(), ResultCode.ERROR_SERVER.getCode()));
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<CommonResponseDTO<Void>> logout(HttpSession session) {
        try {
            userService.logout(session);
            return ResponseEntity.ok(
                    CommonResponseDTO.successNoData()
            );
        } catch (CustomException ce) {
            int code = mapErrorCodeToResultCode(ce.getErrorCode());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseDTO.failure(ce.getErrorCode().getCode(), code));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDTO.failure(ResultCode.ERROR_SERVER.getMessage(), ResultCode.ERROR_SERVER.getCode()));
        }
    }

    /**
     * ErrorCode -> ResultCode 매핑
     * */
    private int mapErrorCodeToResultCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND -> ResultCode.FAIL_DATA_ERROR.getCode();         // -1
            case UNAUTHORIZED, ACCESS_DENIED, LOGIN_FAILURE -> ResultCode.INVAILD_SESSION_ERROR.getCode();           // -21
            case INVALID_PASSWORD_FORMAT, INVALID_USERNAME_FORMAT,
                 DUPLICATE_USERNAME, PASSWORD_MISMATCH ->
                    ResultCode.FAIL_INVALID_PARAMETER.getCode();                 // -9
//            case TOO_MANY_REQUEST ->
//                    ResultCode.FAIL_TOO_MANY_REQUEST.getCode();                  // -91
            default -> ResultCode.ERROR_SERVER.getCode();                        // -99
        };
    }
}