CREATE TABLE round (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   round_num INT NOT NULL, -- 라운드 번호
   round_date DATE NOT NULL, -- 라운드 날짜
   round_start_time DATETIME, -- 라운드 시작 시간
   round_end_time DATETIME, -- 라운드 종료 시간
   game_start_time DATETIME, -- 게임 진행 시작 시간
   game_end_time DATETIME, -- 게임 진행 종료 시간
   game_start_price DECIMAL(18,8), -- 라운드 시작 가격
   game_end_price DECIMAL(18,8), -- 라운드 종료 가격 (게임 종료 후 설정)
   round_result VARCHAR(20) NOT NULL, -- 라운드 결과 (ENUM)
   round_status VARCHAR(20) NOT NULL, -- 라운드 상태 (ENUM)
   INDEX idx_round_date_num (round_date, round_num)
);