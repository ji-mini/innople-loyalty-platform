package com.innople.loyalty.domain.points;

public enum PointEventType {
    EARN,
    USE,
    EXPIRE_AUTO,
    EXPIRE_MANUAL,
    ADJUST_EARN,
    ADJUST_USE,
    // 회원 최종 탈회(WITHDRAWN) 시 잔여 포인트 전량 소각.
    BURN_WITHDRAW
}

