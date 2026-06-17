package com.innople.loyalty.service.member;

import com.innople.loyalty.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitialPasswordLinkSender {

    private final JavaMailSender mailSender;

    @Value("${app.member.initial-password-link.login-url}")
    private String loginUrl;

    @Value("${app.member.initial-password-link.from}")
    private String from;

    public void send(Member member, String initialPassword) {
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new IllegalArgumentException("초기 비밀번호 링크를 발송하려면 이메일이 필요합니다.");
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalArgumentException("초기 비밀번호 링크를 발송하려면 초기 비밀번호가 필요합니다.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(member.getEmail().trim());
        message.setSubject("[INNOPLE] 앱 로그인 초기 비밀번호 안내");
        message.setText("""
                안녕하세요, %s님.

                앱 로그인 계정이 생성되었습니다.

                로그인 링크: %s
                로그인 ID: %s
                초기 비밀번호: %s

                보안을 위해 로그인 후 비밀번호를 변경해주세요.
                """.formatted(
                member.getName(),
                loginUrl,
                member.getPhoneNumber(),
                initialPassword
        ));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException("초기 비밀번호 링크 발송에 실패했습니다.", e);
        }
    }
}
