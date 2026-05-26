package com.innople.loyalty.service.memberauth;

public interface MemberAuthService {
    MemberAuthResult signup(MemberSignupCommand command);

    MemberAuthResult login(MemberLoginCommand command, MemberLoginContext context);
}
