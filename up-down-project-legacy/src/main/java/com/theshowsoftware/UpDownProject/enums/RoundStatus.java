package com.theshowsoftware.UpDownProject.enums;

public enum RoundStatus {
    BETTING, // 베팅 가능 시간 
    LOCKED, // 베팅 닫힘 시간
    PROCESSING, // 게임 진행 시간
    FINISHED; //  결과 정산 시간

    public RoundStatus getNextStatus() {
        switch (this) {
            case BETTING: return LOCKED;
            case LOCKED: return PROCESSING;
            case PROCESSING: return FINISHED;
            default: throw new IllegalStateException("마지막 상태입니다.");
        }
    }
}