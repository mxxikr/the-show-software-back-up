# 개발 문서

---

## 기술 스택

- **Language & Framework**
  - Java 21
  - Spring Framework 3.4.3
- **Build Tool**
  - Gradle 8.5
- **Database**
  - Spring Data JPA, Maria DB
- **WebSocket**
  - STOMP + SOCKJS

## 거래소 API 정보

- Binance API
  - `https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT`
- Okx API
  - `https://www.okx.com/api/v5/market/ticker?instId=BTC-USDT`
- Huobi API
  - `https://api.huobi.pro/market/trade?symbol=btcusdt`

## **WebSocket 통신 정보**

- **연결 정보**


    | 항목 | 설명 |
    | --- | --- |
    | **WebSocket 연결 URL** | `https://58.120.209.54/up_down_project/udws` |
    | **사용 프로토콜** | STOMP + SockJS |
    | **요청 경로 (실시간 가격)** | `/pub/price/current` |
    | **구독 경로 (실시간 가격)** | `/user/{sessionId}/price/current` |
    | **요청 경로 (최근 100초 가격 내역)** | `/pub/price/history` |
    | **구독 경로 (최근 100초 가격 내역)** | `/user/{sessionId}/price/history` |
    | **요청 경로 (라운드 시간 정보)** | `/pub/round/info` |
    | **구독 경로 (라운드 시간 정보)** | `/user/{sessionId}/round/info` |
    | **요청 경로 (라운드 게임 시작 가격)** | `/pub/round/prices/start` |
    | **구독 경로 (라운드 게임 시작 가격)** | `/user/{sessionId}/round/prices/start` |
    | **요청 경로 (라운드 게임 종료 가격)** | `/pub/round/prices/end` |
    | **구독 경로 (라운드 게임 종료 가격)** | `/user/{sessionId}/round/prices/end` |
    - **서버는 항상 0.5초마다 Binance, Okx, Huobi API에서 실시간 데이터 수집**
        - 원본 데이터는 DB 저장
        - 클라이언트 전송 및 게임 시작, 종료 시 판별 하는 값 계산
            - 평균 가격 + 각 거래소의 가격 차이 절대 값을 랜덤으로 선택한 후 해당 값의 4분의 1 범위를 랜덤으로 선택한 값
    - 구독 후 클라이언트가 `/pub/price/history` 요청을 보내면 **해당 클라이언트에게 최근 100초 데이터를 즉시 전송**
    - 구독 후 클라이언트가 `/pub/price/current`로 요청을 보내면 **해당 클라이언트에게 0.5초마다 실시간 데이터를 전송**
    - 구독 후 클라이언트가 `/pub/round/info` 로 요청을 보내면 **해당 클라이언트에게 현재 진행 중인 라운드의 게임 시작 시간(예측), 종료 시간(예측), 서버 현재 시간을 전송**
    - 구독 후 클라이언트가 `/pub/round/prices/start` 로 요청을 보내면 **해당 클라이언트에게 현재 진행 중인 라운드의 게임 진행 시작 가격 데이터 전송**
    - 구독 후 클라이언트가 `/pub/round/prices/end` 로 요청을 보내면 **해당 클라이언트에게 현재 진행 중인 라운드의 게임 진행 종료 가격 데이터 전송**
        - 구독(Subscribe)은 단순히 세션 활성화를 위한 작업이며, 데이터 전송은 요청 없이 이루어지지 않음
- response Body

    ```json
    {
        "timestamp": 1745255168108, 
        "price": "87572.86000000"
    }
    ```

    ```json
    {
        "serverTimestamp": 1745518839478,
        "prices": [
            {
                "price": "92331.29666667",
                "timestamp": 1745518743260
            },
            {
                "price": "92331.29666667",
                "timestamp": 1745518743791
            },
            {
                "price": "92331.30000000",
                "timestamp": 1745518744260
            },
            {
                "price": "92331.29666667",
                "timestamp": 1745518744766
            },
            {
                "price": "92331.33000000",
                "timestamp": 1745518745258
            },
            {
                "price": "92331.33000000",
                "timestamp": 1745518745758
            },
            {
                "price": "92309.94333333",
                "timestamp": 1745518838374
            },
            {
                "price": "92316.26000000",
                "timestamp": 1745518838757
            },
            {
                "price": "92322.99000000",
                "timestamp": 1745518839258
            }
        ]
    }
    ```

    ```json
    {
        "roundId": 1838,
        "serverTimestamp": 1744160850,
        "gameStartTimestamp": 1744160870,
        "gameEndTimestamp": 1744160875
    }
    
    ```

    ```json
    {  
    	  "roundId":5338,
    		"gameStartPrice":82229.99000000
    }
    ```

    ```json
    {   
    		"roundId":5338,
    		"gameEndPrice":82229.98000000
    }
    ```


## 가격 데이터 관리

### 주요 기능

- API 호출을 통한 실시간 가격 수집 및 클라이언트 전송 및 DB 저장
  - 0.5초 마다 데이터 수집
  - 수집한 데이터 값 계산해 전송
    - 평균 가격 + 각 거래소의 가격 차이 절대 값을 랜덤으로 선택한 후 해당 값의 절반 범위를 랜덤으로 선택한 값
- 최신 100초 데이터를 큐에 저장하고 있다가 클라이언트에게 요청 올 경우 해당 데이터 리스트 전송
- 가격 데이터가 일정 임계 값(50개) 모일 경우 DB에 벌크 저장
- 하루 지난 데이터 매일 자정 5분 후 삭제

### DDL

- btc 실시간 가격 정보 저장 테이블

    ```sql
    CREATE TABLE price (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        symbol VARCHAR(10) NOT NULL, -- BTCUSDT
        price DECIMAL(18,8) NOT NULL, -- 해당 시점의 BTC/USDT 가격
        exchange VARCHAR(10) NOT NULL, -- 거래소 api 구별
        timestamp DATETIME(3) NOT NULL
    );
    ```

    ```json
    {
    	"price": [
    		{
    			"price" : 92192.04,
    			"id" : 1,
    			"timestamp" : "2025-04-24T07:43:48.737Z",
    			"exchange" : "BINANCE",
    			"symbol" : "BTCUSDT"
    		},
    		{
    			"price" : 92202.80,
    			"id" : 2,
    			"timestamp" : "2025-04-24T07:43:48.754Z",
    			"exchange" : "OKX",
    			"symbol" : "BTCUSDT"
    		},
    		{
    			"price" : 92192.50,
    			"id" : 3,
    			"timestamp" : "2025-04-24T07:43:48.738Z",
    			"exchange" : "HUOBI",
    			"symbol" : "BTCUSDT"
    		}
    	]
    }
    ```


## **Round 관리 기능**

### 주요 기능

- **스케쥴링 작업**
  - 스케줄 관리 관련 코드는 `RoundScheduler`에 정의되어 있음
- **라운드 생성**


    | 구간 | 시작 시간 | 종료 시간 | 설명 |
    | --- | --- | --- | --- |
    | **BETTING** | 00:00 | 00:15 | 베팅 받는 시간 |
    | **LOCKED** | 00:15 | 00:20 | 베팅 잠금, 대기 시간 |
    | **PROCESSING** | 00:20 | 00:25 | 게임 진행 시간 |
    | **FINISHED** | 00:25 | 00:30 | 게임 진행 결과 보여주는 시간 |
    | **다음 라운드 시작** | 00:30 | - | 다음 라운드 시작 |
- **라운드 자동 생성**
  - 매 라운드 시작 시, 날짜와 번호(`roundDate`, `roundNum`)를 기준으로 새 라운드를 생성
  - 라운드 번호는 해당 날짜의 기존 최대 값을 기반으로 새 번호를 부여
  - 초기 상태 설정 (`BETTING`)
- **라운드 상태 업데이트**
  - 라운드의 진행 단계(베팅, 잠금, 진행, 종료) 변화에 따라 상태 업데이트
  - 게임 시작 시 시작 가격을 저장하고, 종료 시 종료 가격을 저장하며 최종 결과를 계산(`UP`, `DOWN`, `SAME`)
  - 라운드 결과 계산 및 저장
  - 라운드 시작/종료, 게임 진행 시작/종료 타임스탬프 관리 및 저장
- **라운드 게임 진행 시작, 종료 시간 전송**
  - 클라이언트에게 필요한 라운드 정보(게임 시작/종료 시간 예측 값) 제공
- **라운드 게임 진행 시작, 종료 가격 전송**
  - 클라이언트에게 필요한 라운드 정보(게임 시작/종료 가격) 제공
- **자정 초기화**
  - 매일 자정 이전 날짜의 미 완료 라운드 처리 후 그날의 첫 번째 라운드 자동 생성
- **라운드 삭제**
  - 스케줄러를 이용해 3개월 이상 지난 라운드 데이터를 매일 삭제

### DDL

- 라운드 시간 세팅 정보 저장 테이블

    ```sql
    CREATE TABLE round_setting (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       betting_time INT NOT NULL, -- 배팅 가능 시간
       betting_lock_time INT NOT NULL, -- 배팅 잠금 시간
       processing_time INT NOT NULL, -- 게임 진행 시간
       result_time INT NOT NULL -- 결과 출력 시간
    );
    
    ```

    ```json
    {
    "round_setting": [
    	{
    		"betting_lock_time" : 5,
    		"betting_time" : 15,
    		"processing_time" : 5,
    		"result_time" : 5,
    		"id" : 1
    	}
    ]}
    
    ```

- 라운드 정보 저장 테이블

    ```sql
    CREATE TABLE round (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        round_num INT NOT NULL, -- 라운드 번호
        round_date DATE NOT NULL, -- 라운드 날짜
        round_start_time DATETIME(3), -- 라운드 시작 시간
        round_end_time DATETIME(3), -- 라운드 종료 시간
        game_start_time DATETIME(3), -- 게임 진행 시작 시간
        game_end_time DATETIME(3), -- 게임 진행 종료 시간
        game_start_price DECIMAL(18,8), -- 라운드 시작 가격
        game_end_price DECIMAL(18,8), -- 라운드 종료 가격 (게임 종료 후 설정)
        round_result VARCHAR(20) NOT NULL, -- 라운드 결과 (ENUM)
        round_status VARCHAR(20) NOT NULL, -- 라운드 상태 (ENUM)
        INDEX idx_round_date_num (round_date, round_num),
        INDEX idx_round_status_id ON round (round_status, id DESC),
        INDEX idx_round_date_status ON round (round_date, round_status)
      );
    ```

    ```json
    {
    "round": [
    	{
    		"game_end_price" : 79675.37,
    		"game_start_price" : 79692.00,
    		"round_date" : "2025-04-08",
    		"round_num" : 1,
    		"id" : 1,
    		"game_end_time" : "2025-04-08 14:18:48.467",
    		"game_start_time" : "2025-04-08 14:18:43.475",
    		"round_end_time" : "2025-04-08 14:18:53.392",
    		"round_start_time" : "2025-04-08 14:18:23.402",
    		"round_result" : "DOWN",
    		"round_status" : "FINISHED"
    	},
    	{
    		"game_end_price" : 79687.84,
    		"game_start_price" : 79662.00,
    		"round_date" : "2025-04-08",
    		"round_num" : 2,
    		"id" : 2,
    		"game_end_time" : "2025-04-08 14:19:18.475",
    		"game_start_time" : "2025-04-08 14:19:13.456",
    		"round_end_time" : "2025-04-08 14:19:23.398",
    		"round_start_time" : "2025-04-08 14:18:53.396",
    		"round_result" : "UP",
    		"round_status" : "FINISHED"
    	}
    ]}
    
    ```


## **API 명세서**

### WebSocket 관련 - 직전 **100**초 가격 기록 요청

- **Endpoint**
  - `/pub/price/history`
- **WebSocket Method**
  - `MessageMapping`
- **Response**

    ```json
    {
        "serverTimestamp": 1745518839478,
        "prices": [
            {
                "price": "92331.29666667",
                "timestamp": 1745518743260
            },
            {
                "price": "92331.29666667",
                "timestamp": 1745518743791
            },
            {
                "price": "92331.30000000",
                "timestamp": 1745518744260
            },
            {
                "price": "92331.29666667",
                "timestamp": 1745518744766
            },
            {
                "price": "92331.33000000",
                "timestamp": 1745518745258
            },
            {
                "price": "92331.33000000",
                "timestamp": 1745518745758
            },
            {
                "price": "92309.94333333",
                "timestamp": 1745518838374
            },
            {
                "price": "92316.26000000",
                "timestamp": 1745518838757
            },
            {
                "price": "92322.99000000",
                "timestamp": 1745518839258
            }
        ]
    }
    ```


### WebSocket 관련 - 실시간 가격 요청

- **Endpoint**
  - `/pub/price/current`
- **WebSocket Method**
  - `MessageMapping`
- **Response**

    ```json
    {
        "timestamp": 1745253276763,
        "price": "87509.44000000"
    }
    ```


### WebSocket 관련 - 라운드 게임 시간 요청

- **Endpoint**
  - `/pub/round/info`
- **WebSocket Method**
  - `MessageMapping`
- **Response**

    ```json
    {
    	  "roundId": 12,
    	  "serverTimestamp": 1698145200000,
    	  "gameStartTimestamp": 1698145310000,
    	  "gameEndTimestamp": 1698145370000
    }
    
    ```


### WebSocket 관련 - 라운드 게임 시작 가격 요청

- **Endpoint**
  - `/pub/round/prices/start`
- **WebSocket Method**
  - `MessageMapping`
- **Response**

    ```json
    {
    		"roundId":5338,
    		"gameStartPrice":82229.99000000
    }
    ```


### WebSocket 관련 - 라운드 게임 종료 가격 요청

- **Endpoint**
  - `/pub/round/prices/end`
- **WebSocket Method**
  - `MessageMapping`
- **Response**

    ```json
    {
    		"roundId":5338,
    		"gameEndPrice":82229.99000000
    }
    ```


### **라운드 설정 관리 API - 현재 라운드 설정 조회**

- **Endpoint**
  - `/api/admin/get_round_settings`
- **Method**
  - `GET`
- **Headers**
  - `Content-Type`: application/json
- **Response**

    ```json
    {
        "bettingTime": 15,
        "bettingLockTime": 5,
        "processingTime": 5,
        "resultTime": 5
    }
    
    ```


### **라운드 설정 관리 API - 라운드 설정 업데이트**

- **Endpoint**
  - `/api/admin/update_round_settings`
- **Method**
  - `POST`
- **Headers**
  - `Content-Type`: application/json
- **Request**

    ```json
    {
        "bettingTime":15,
        "bettingLockTime":5,
        "processingTime":5,
        "resultTime":5
    }
    
    ```

- **Response**

    ```json
    {
        "bettingTime": 15,
        "bettingLockTime": 5,
        "processingTime": 5,
        "resultTime": 5
    }
    
    ```


### **라운드 조회 API - 현재 진행 중인 라운드 조회**

- **Endpoint**
  - `/api/game/updown/get_current_round`
- **Method**
  - `GET`
- **Headers**
  - `Content-Type`: application/json
- **Response**

    ```json
    {
        "data": {
            "id": 3,
            "roundNum": 3,
            "gameStartTime": null,
            "gameEndTime": null,
            "roundStartTime": "2025-04-08 17:37:23.837",
            "roundEndTime": null,
            "gameStartPrice": null,
            "gameEndPrice": null,
            "roundStatus": "BETTING",
            "roundDate": "2025-04-08",
            "roundResult": null
        },
        "result_code": 1,
        "message": "성공"
    }
    
    ```

    ```json
    {
      "result_code": 0,
      "message": "데이터 없음",
      "data": null
    }
    
    ```


### **라운드 조회 API - 최근 종료된 라운드 정보 조회**

- **Endpoint**
  - `/api/game/updown/get_latest_finished`
- **Method**
  - `GET`
- **Headers**
  - `Content-Type`: application/json
- **Response**

    ```json
    {
        "data": {
            "id": 46025,
            "roundNum": 2197,
            "gameStartTime": "2025-04-24 18:18:08.416",
            "gameEndTime": "2025-04-24 18:18:13.409",
            "roundStartTime": "2025-04-24 18:17:48.326",
            "roundEndTime": "2025-04-24 18:18:18.300",
            "gameStartPrice": 92337.98,
            "gameEndPrice": 92347.07,
            "roundStatus": "FINISHED",
            "roundDate": "2025-04-24",
            "roundResult": "UP"
        },
        "result_code": 1,
        "message": "성공"
    }
    ```

    ```json
    {
      "result_code": 0,
      "message": "데이터 없음",
      "data": null
    }
    
    ```


### **라운드 조회 API - 특정 날짜 및 특정 라운드 조회**

- **Endpoint**
  - `/api/game/updown/get_round/{roundDate}/{roundNum}`
- **Method**
  - `GET`
- **Path Variables**
  - `roundDate`: YYYY-MM-DD 형식의 날짜
  - `roundNum`: 조회할 라운드 번호
- **Headers**
  - `Content-Type`: application/json
- **Response**

    ```json
    {
        "data": {
            "id": 2,
            "roundNum": 2,
            "gameStartTime": null,
            "gameEndTime": null,
            "roundStartTime": "2025-04-09 16:58:25.566",
            "roundEndTime": null,
            "gameStartPrice": null,
            "gameEndPrice": null,
            "roundStatus": "BETTING",
            "roundDate": "2025-04-09",
            "roundResult": null
        },
        "result_code": 1,
        "message": "성공"
    }
    
    ```

    ```json
    {
      "result_code": -2,
      "error_message": "입력 파라미터 오류"
    }
    
    ```


## Result Code 정의

- **1**: 데이터 조회 성공
- **0**: 데이터 없음
- **2**: 입력 파라미터 오류
- **3**: 서버 오류