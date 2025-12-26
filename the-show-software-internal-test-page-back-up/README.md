# API 정보


## 접속 정보

- DEV
    - https://192.168.0.11/internal_test_page/

## 공통 Response 구조 및 ResultCode

### Request Headers

- **Content-type**
    - application/json; charset=utf-8
- Response Headers
    - **Content-type**
        - application/json; charset=utf-8
    - **Body**
        - `result_code`
            - **Integer**
                - 서버에서 반환하는 처리 결과 코드
        - `data`
            - **JsonObject**
                - 성공 시 실제 데이터 반환
                - 데이터가 없으면 null 반환
        - `errorMessage`
            - **String**
                - 오류 시 에러 메시지 반환
            - 성공 시 null 반환
- ResultCode 값


    | result_code | Description |
    | --- | --- |
    | 1 | 데이터가 존재하는 정상 처리 (SUCCESS_HAS_DATA) |
    | 0 | 데이터가 없는 정상 처리 (SUCCESS_NO_DATA) |
    | -1 | 데이터 조회 실패/오류 (FAIL_DATA_ERROR) |
    | -9 | 입력 파라미터 오류 (FAIL_INVALID_PARAMETER) |
    | -21 | 회원 토큰 정보 오류 (FAIL_TOKEN_ERROR) |
    | -91 | 너무 많은 호출 시도 (FAIL_TOO_MANY_REQUEST) |
    | -99 | 서버 내부 오류 (ERROR_SERVER) |

## 서버와 클라이언트 간 세션 인증 구조

- **서버가 인증 상태를 관리하고 클라이언트는 발급받은 쿠키(세션 ID)를 통해 인증 상태를 유지**
    1. 로그인 시 서버에서 세션을 만들고, 해당 세션 ID를 쿠키로 전달
    2. 요청마다 쿠키로 인증 정보를 확인
- **서버 측 인증 확인**
    - 서버는 수신한 쿠키의 세션 ID를 세션 저장소에서 찾아 유효한 세션인지, 로그인 상태인지 식별함
    - 유효한 세션이면 인증된 사용자로 간주하여 API를 정상 처리
    - 세션이 만료 혹은 무효화된 경우 401 Unauthorized 등으로 응답해 재로그인을 요청
- **세션 타임아웃 시간**
    - 30분

<br></br>

# API 명세서

## 회원 가입 (Sign Up)

- URL
    - `POST`
    - `/user_info/sign_up`
- Request Body
    - JSON

        ```json
        {
          "userName": String, // 아이디
          "password": String, // 비밀번호
        }
        ```

- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": {
            "userId": Long, // 회원 고유 id
            "userName": String // 아이디
          }
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": {
                "userId": 1313,
                "userName": "testApi"
            }
        
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | **INVALID_USERNAME_FORMAT** | 아이디 형식이 잘못되었습니다. 영문/숫자/._-만 허용하며, 최대 32바이트입니다. |
            | **DUPLICATE_USERNAME** | 이미 존재하는 회원입니다. |
            | **INVALID_PASSWORD_FORMAT** | 비밀번호 형식이 잘못되었습니다. 영문/숫자 및 특정 특수문자만 허용하며, 6자 이상 64바이트 이하입니다. |

## 로그인 (Login)

- URL
    - `POST`
    - `/user_info/login`
- Request Body

    ```json
    {
      "userName": String,  // 아이디
      "password": String   // 비밀번호
    }
    ```

- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": {
              "userId": Long,      // 회원 고유 id
              "userName": String  // 아이디
        	 }
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": {
                "userName": "testApi",
                "userId": 1313
            }
        }
        ```

        - Response Header
            - `Set-Cookie: JSESSIONID=랜덤값`
    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | **INVALID_USERNAME_FORMAT** | 아이디 형식이 잘못되었습니다. 영문/숫자/._-만 허용하며, 최대 32바이트입니다. |
            | **INVALID_PASSWORD_FORMAT** | 비밀번호 형식이 잘못되었습니다. 영문/숫자 및 특정 특수문자만 허용하며, 6자 이상 64바이트 이하입니다. |
            | **PASSWORD_MISMATCH** | 비밀번호가 일치하지 않습니다. |
            | **USER_NOT_FOUND** | 존재하지 않는 회원입니다. |
            | **INVALID_INFORMATION** | 유효하지 않은 로그인 정보입니다. |
- 참고 사항
    - 서버 세션이 생성되어, 이후 API 호출은 세션 인증 통해 진행

## 로그아웃 (Logout)

- URL
    - `POST`
    - `/user_info/logout`
- Request Body
- Response Body
    - 성공 시

        ```json
        {
            "result_code": Integer
        }
        ```

        ```json
        {
            "result_code": 0
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지

      | **코드** | **설명** |
              | --- | --- |
      | **UNAUTHORIZED** | 인증되지 않은 요청입니다. |
- 참고 사항
    - 서버 세션이 무효화되어, 이후 API 호출 시 다시 로그인해야 함

## 전체 회원 조회 (Find All Users)

- URL
    - `GET`
    - `/user_info/find_user_all`
- Request Body
- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": [
            {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "password": String,  // 비밀번호
              "createdDate": LocalDateTime,    // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
            },
            {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "password": String,  // 비밀번호
              "createdDate": LocalDateTime,    // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
            },
            {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "password": String,  // 비밀번호
              "createdDate": LocalDateTime,    // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
            }
          ]
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": [
                {
                    "userId": 6,
                    "userName": "testApi",
                    "userAmount": 0.0,
                    "createdDate": "2025-02-17T06:53:30.937543",
                    "lastModifiedDate": "2025-02-17T06:53:30.937543"
                },
                {
                    "userId": 7,
                    "userName": "testApi2",
                    "userAmount": 0.0,
                    "createdDate": "2025-02-17T09:11:35.122466",
                    "lastModifiedDate": "2025-02-17T09:11:35.122466"
                },
                {
                    "userId": 8,
                    "userName": "testApi3",
                    "userAmount": 0.0,
                    "createdDate": "2025-03-13T06:46:04.039073",
                    "lastModifiedDate": "2025-03-13T06:46:04.039073"
                }
            ]
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | **INVALID_INFORMATION** | 유효하지 않은 로그인 정보입니다. |
            | **USER_NOT_FOUND** | 존재하지 않는 회원입니다. |

## 특정 사용자 조회 - ID

- URL
    - `GET`
    - `/user_info/find_user_by_id`
- Request Body

    ```json
    {
      "userId": Long // 회원 고유 id
    }
    ```

- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "createdDate": LocalDateTime, // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
        	 }
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": {
                "userId": 6,
                "userName": "testApi",
                "userAmount": 0.0,
                "createdDate": "2025-02-17T06:53:30.937543",
                "lastModifiedDate": "2025-02-17T06:53:30.937543"
            }
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | **INVALID_INFORMATION** | 유효하지 않은 로그인 정보입니다. |
            | **USER_NOT_FOUND** | 존재하지 않는 회원입니다. |

## 특정 사용자 조회 - 사용자 명

- URL
    - `GET`
    - `/user_info/find_user_by_name`
- Request Body

    ```json
    {
      "userName": String // 아이디
    }
    ```

- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "createdDate": LocalDateTime,    // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
        	 }
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": {
                "userId": 6,
                "userName": "testApi",
                "userAmount": 0.0,
                "createdDate": "2025-02-17T06:53:30.937543",
                "lastModifiedDate": "2025-02-17T06:53:30.937543"
            }
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | INVALID_USERNAME_FORMAT | 아이디 형식이 잘못되었습니다. 영문/숫자/._-만 허용하며, 최대 32바이트입니다.
             |
            | **USER_NOT_FOUND** | 존재하지 않는 회원입니다. |

## 내 정보 조회 (My Info)

- URL
    - `GET`
    - `/user_info/my_info`
- Request Body
- Response Body
    - 성공 시

        ```json
        {
          "result_code": Integer,
          "data": {
              "userId": Long,      // 회원 고유 id
              "userName": String,  // 아이디
              "userAmount": Double, // 보유 금액
              "createdDate": LocalDateTime,    // 생성 시간
              "lastModifiedDate": LocalDateTime // 수정 시간
        	 }
        }
        ```

        ```json
        {
            "result_code": 1,
            "data": {
                "userId": 6,
                "userName": "testApi",
                "userAmount": 0.0,
                "createdDate": "2025-02-17T06:53:30.937543",
                "lastModifiedDate": "2025-02-17T06:53:30.937543"
            }
        }
        ```

    - 실패 시

        ```json
        {
            "result_code": Integer,
            "error_message": String
        }
        ```

        - 에러 메세지


            | **코드** | **설명** |
            | --- | --- |
            | **INVALID_USERNAME_FORMAT** | 아이디 형식이 잘못되었습니다. 영문/숫자/._-만 허용하며, 최대 32바이트입니다. |
            | **DUPLICATE_USERNAME** | 이미 존재하는 회원입니다. |
            | **INVALID_PASSWORD_FORMAT** | 비밀번호 형식이 잘못되었습니다. 영문/숫자 및 특정 특수문자만 허용하며, 6자 이상 64바이트 이하입니다. |
            | **PASSWORD_MISMATCH** | 비밀번호가 일치하지 않습니다. |
            | **USER_NOT_FOUND** | 존재하지 않는 회원입니다. |
- 참고 사항
    - 세션이 유효하지 않으면, 인증되지 않은 사용자로 처리되어 에러 발생