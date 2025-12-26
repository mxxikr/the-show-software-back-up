# 개발 문서

---

## 기술 스택

- **Language & Framework**
  - Java 21
  - Spring Framework 3.5.0
- **Build Tool**
  - Gradle 8.5

## **주요 기능**

### **Tick 데이터 관리**

- 틱 데이터를 실시간으로 수집 및 캐싱
- 특정 시간 범위에서 틱 데이터를 조회
- WebSocket으로 스트리밍

### **Candle 데이터 관리**

- 다양한 차트 유형으로 캔들 데이터를 생성
- 실시간 캔들 데이터 계산 및 업데이트
- 특정 시간 범위에서 캔들 데이터를 조회

### **WebSocket 기반 데이터 전송**

- ChartType(차트 유형) 및 SymbolType(심볼 타입)에 따른 데이터 전송
- 클라이언트-서버 간 실시간 통신 제공

### **에러 핸들링**

- 프로젝트 전역에서 발생하는 에러를 공통적으로 처리
- Custom Exception 및 에러 코드 관리

## **데이터 모델**

### **`TickPacketDTO`**

- `price`
  - 틱 가격
  - BigInteger Type
- `quantity`
  - 거래량
  - Long Type
- `timestamp`
  - 데이터 수집 시간
  - Long Type
- 메서드
  - `toBytes()`
    - 틱 데이터를 바이트 배열로 변환
  - `fromBytes(ByteBuffer)`
    - 바이트 데이터를 틱 데이터로 복원

### **`CandlePacketDTO`**

- `symbolType`
  - 심볼 타입
  - SymbolType Type
- `chartType`
  - 차트 유형
  - ChartType Type
- `startPrice`
  - 시가
  - BigInteger Type
- `highPrice`
  - 고가
  - BigInteger Type
- `lowPrice`
  - 저가
  - BigInteger Type
- `endPrice`
  - 종가
  - BigInteger Type
- `quantity`
  - 거래량
  - Long Type
- `tickCount`
  - 변동 횟수
  - int Type
- `candleStartTime`
  - 캔들 시작 시간
  - Long Type
- `candleEndTime`
  - 캔들 종료 시간
  - Long Type

## Enum 정의

### ChartType

| **차트 유형** | **hexCode** | **시간 단위** | **간격 크기** | **설명** |
| --- | --- | --- | --- | --- |
| **TICK** | `0x00` | 초(`SECONDS`) | 1 | 틱 데이터 |
| **ONE_SECOND** | `0x01` | 초(`SECONDS`) | 1 | 1초 봉 |
| **THREE_SECONDS** | `0x02` | 초(`SECONDS`) | 3 | 3초 봉 |
| **FIVE_SECONDS** | `0x03` | 초(`SECONDS`) | 5 | 5초 봉 |
| **TEN_SECONDS** | `0x04` | 초(`SECONDS`) | 10 | 10초 봉 |
| **THIRTY_SECONDS** | `0x05` | 초(`SECONDS`) | 30 | 30초 봉 |
| **ONE_MINUTE** | `0x06` | 분(`MINUTES`) | 1 | 1분 봉 |
| **THREE_MINUTES** | `0x07` | 분(`MINUTES`) | 3 | 3분 봉 |
| **FIVE_MINUTES** | `0x08` | 분(`MINUTES`) | 5 | 5분 봉 |
| **TEN_MINUTES** | `0x09` | 분(`MINUTES`) | 10 | 10분 봉 |
| **FIFTEEN_MINUTES** | `0x0A` | 분(`MINUTES`) | 15 | 15분 봉 |
| **ONE_HOUR** | `0x0B` | 시간(`HOURS`) | 1 | 1시간 봉 |
| **THREE_HOURS** | `0x0C` | 시간(`HOURS`) | 3 | 3시간 봉 |
| **FIVE_HOURS** | `0x0D` | 시간(`HOURS`) | 5 | 5시간 봉 |
| **TWELVE_HOURS** | `0x0E` | 시간(`HOURS`) | 12 | 12시간 봉 |
| **ONE_DAY** | `0x0F` | 일(`DAYS`) | 1 | 1일 봉 |
| **THREE_DAYS** | `0x10` | 일(`DAYS`) | 3 | 3일 봉 |
| **ONE_WEEK** | `0x11` | 일(`DAYS`) | 7 | 1주 봉 |
| **ONE_MONTH** | `0x12` | 달(`MONTHS`) | 1 | 1달 봉 |

### SymbolType

| **심볼 이름** | **hexCode** | **설명** |
| --- | --- | --- |
| `USDT` | `0x0000` | 테더(USD Stablecoin) |
| `A` | `0x0001` | A 심볼 |
| `AAVE` | `0x0002` | AAVE 토큰 |
| `ADA` | `0x0003` | ADA |
| `ANIME` | `0x0004` | ANIME 토큰 |
| `APE` | `0x0005` | ApeCoin |
| `APT` | `0x0006` | Aptos |
| `ARB` | `0x0007` | Arbitrum |
| `AVAX` | `0x0008` | AVAX |
| `BNB` | `0x0009` | 바이낸스 코인 |
| `BTC` | `0x000A` | 비트코인 |
| `CAKE` | `0x000B` | PancakeSwap 토큰 |
| `COMP` | `0x000C` | Compound |
| `CRV` | `0x000D` | Curve DAO 토큰 |
| `DEGO` | `0x000E` | DEGO 토큰 |
| `DEXE` | `0x000F` | Dexe 토큰 |
| `DOGE` | `0x0010` | DOGE |
| `DOT` | `0x0011` | DOT |
| `DYDX` | `0x0012` | dYdX |
| `EIGEN` | `0x0013` | Eigen Layer Token |
| `ENA` | `0x0014` | ENA 토큰 |
| `ETH` | `0x0015` | ETH |
| `ETHFI` | `0x0016` | EthereumFI |
| `FDUSD` | `0x0017` | FDUSD |
| `FLOKI` | `0x0018` | FLOKI |
| `HBAR` | `0x0019` | Hedera Hashgraph |
| `HUMA` | `0x001A` | HUMA |
| `ICP` | `0x001B` | Internet Computer |
| `INIT` | `0x001C` | INIT 심볼 |
| `LINK` | `0x001D` | Chainlink |
| `LPT` | `0x001E` | Livepeer |
| `LTC` | `0x001F` | LTC |
| `MASK` | `0x0020` | Mask Network |
| `MKR` | `0x0021` | Maker 토큰 |
| `NEAR` | `0x0022` | Near Protocol |
| `NEIRO` | `0x0023` | NEIRO |
| `NXPC` | `0x0024` | NXPC |
| `ONDO` | `0x0025` | Ondo Finance |
| `ORDI` | `0x0026` | Ordi 토큰 |
| `PAXG` | `0x0027` | Pax Gold |
| `PENDLE` | `0x0028` | Pendle 토큰 |
| `PENGU` | `0x0029` | Pengu Token |
| `PEPE` | `0x002A` | Pepe 코인 |
| `PNUT` | `0x002B` | Peanut 토큰 |
| `RENDER` | `0x002C` | Render Token |
| `S` | `0x002D` | S 심볼 |
| `SOL` | `0x002E` | SOL |
| `SOPH` | `0x002F` | SOPH 토큰 |
| `SUI` | `0x0030` | SUI |
| `SYRUP` | `0x0031` | SYRUP |
| `TAO` | `0x0032` | TAO 토큰 |
| `TON` | `0x0033` | TON |
| `TRB` | `0x0034` | Tellor Tributes |
| `TRUMP` | `0x0035` | TRUMP 코인 |
| `TRX` | `0x0036` | TRON, TRX |
| `UNI` | `0x0037` | UNI |
| `USDC` | `0x0038` | USD Coin |
| `VANA` | `0x0039` | VANA |
| `VIRTUAL` | `0x003A` | Virtual Asset |
| `WBTC` | `0x003B` | Wrapped BTC |
| `WCT` | `0x003C` | WCT Token |
| `WIF` | `0x003D` | WIF Token |
| `WLD` | `0x003E` | Worldcoin |

## **주요 서비스**

### **`ChartCacheService`**

- Tick 및 Candle 데이터를 캐싱하고 관리하는 서비스
  - 틱 데이터 추가 및 조회
  - 캔들 데이터 동적 생성 및 업데이트
  - 데이터 캐시 크기 제한 관리
  - 동시 성을 보장하기 위한 심볼별 읽기/쓰기 잠금 처리
  - 캐시 크기 제한
    - Tick 데이터
      - 최대 20,000,000 저장
    - Candle 데이터
      - ChartType별 유지 기간에 따라 다름

  | **ChartType** | **최대 저장 가능 개수** | **설명** |
  | --- | --- | --- |
  | **TICK** | 604,800 | 틱 데이터 (약 1주) |
  | **ONE_SECOND** | 604,800 | 1초 봉 (약 1주) |
  | **THREE_SECONDS** | 201,600 | 3초 봉 (약 1주) |
  | **FIVE_SECONDS** | 120,960 | 5초 봉 (약 1주) |
  | **TEN_SECONDS** | 60,480 | 10초 봉 (약 1주) |
  | **THIRTY_SECONDS** | 20,160 | 30초 봉 (약 1주) |
  | **ONE_MINUTE** | 10,080 | 1분 봉 (약 1주) |
  | **THREE_MINUTES** | 3,360 | 3분 봉 (약 1주) |
  | **FIVE_MINUTES** | 2,016 | 5분 봉 (약 1주) |
  | **TEN_MINUTES** | 1,008 | 10분 봉 (약 1주) |
  | **FIFTEEN_MINUTES** | 672 | 15분 봉 |
  | **ONE_HOUR** | 720 | 1시간 봉 (약 30일) |
  | **THREE_HOURS** | 1,440 | 3시간 봉 (약 2개월) |
  | **FIVE_HOURS** | 1,296 | 5시간 봉 (약 3개월) |
  | **TWELVE_HOURS** | 360 | 12시간 봉 (약 6개월) |
  | **ONE_DAY** | 365 | 1일 봉 (약 1년) |
  | **THREE_DAYS** | 122 | 3일 봉 (약 1년) |
  | **ONE_WEEK** | 52 | 1주 봉 (약 1년) |
  | **ONE_MONTH** | 12 | 1달 봉 (약 1년) |

### **`PacketManager`**

- WebSocket 통신용 패킷 생성 및 파싱 유틸리티
  - 틱 및 캔들 데이터를 패킷 형식으로 생성
  - 생성된 패킷을 클라이언트로 스트리밍
  - 수신된 패킷을 DTO(`TickPacketDTO`, `CandlePacketDTO`)로 변환
- **패킷 구조**
  - 틱 데이터 패킷 포맷

      ```
      !<심볼>; <기준통화>; <패킷유형>; <차트타입>; <시세데이터>#
      ```

    - ex) !SOL;USDT;0x01;0x08;56000000000#
  - 캔들 데이터 패킷 포맷

      ```
      !<코인코드>; <기준통화>; <패킷유형>; <캔들데이터>#
      ```

    - ex) !SOL;USDT;0x02;5m;1633018400000;1633018700000;55000000000;56000000000#

### **`PacketSenderService`**

- 클라이언트에게 WebSocket 응답 전송을 처리하는 서비스

### **`ChartScheduler`**

- Tick 데이터를 일정 간격으로 자동 생성 및 처리하는 스케줄러
  - 정해진 시간 간격으로 Tick 데이터 생성 및 ChartCacheService에 저장
  - 캔들 데이터를 ChartType에 따라 주기적으로 업데이트

## **예외 처리**

### **CustomException**

- 예외가 필요한 경우 `CustomException`과 함께 `ErrorCode`를 지정
- 응답 구조는 `CommonResponseDTO` 형식

    ```json
    {  
    	 "success": false,  
    	 "errorCode": 249,  
    	 "message": "유효하지 않은 패킷 구조체입니다."
     }
    ```


### Result Code 정의

- **1**: 데이터 조회 성공
- **0**: 데이터 없음
- **2**: 입력 파라미터 오류
- **3**: 서버 오류