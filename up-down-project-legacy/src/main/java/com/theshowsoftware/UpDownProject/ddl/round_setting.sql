CREATE TABLE round_setting (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   betting_time INT NOT NULL, -- 배팅 가능 시간
   betting_lock_time INT NOT NULL, -- 배팅 잠금 시간
   processing_time INT NOT NULL, -- 게임 진행 시간
   result_time INT NOT NULL -- 결과 출력 시간
);