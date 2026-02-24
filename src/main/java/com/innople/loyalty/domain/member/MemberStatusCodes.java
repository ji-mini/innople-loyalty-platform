package com.innople.loyalty.domain.member;

public final class MemberStatusCodes {
    private MemberStatusCodes() {
    }

    public static final String GROUP = "MEMBER_STATUS";

    public static final String NORMAL = "NORMAL";
    public static final String DORMANT = "DORMANT";
    public static final String WITHDRAW_REQUESTED = "WITHDRAW_REQUESTED";
    public static final String WITHDRAWN = "WITHDRAWN";
}

