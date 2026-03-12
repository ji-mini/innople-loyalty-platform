package com.innople.loyalty.domain.member;

public final class MemberStatusCodes {
    private MemberStatusCodes() {
    }

    public static final String GROUP = "MEMBER_STATUS";

    public static final String ACTIVE = "ACTIVE";
    public static final String DORMANT = "DORMANT";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String WITHDRAWN = "WITHDRAWN";

    // Legacy codes (backward compatibility)
    public static final String LEGACY_NORMAL = "NORMAL";
    public static final String LEGACY_WITHDRAW_REQUESTED = "WITHDRAW_REQUESTED";
}

