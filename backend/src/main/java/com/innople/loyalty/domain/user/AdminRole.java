package com.innople.loyalty.domain.user;

public enum AdminRole {
    OPERATOR(10),
    ADMIN(20),
    SUPER_ADMIN(30);

    private final int level;

    AdminRole(int level) {
        this.level = level;
    }

    public boolean atLeast(AdminRole required) {
        return this.level >= required.level;
    }
}

