package com.miracle.coordifit.auth.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.miracle.coordifit.auth.model.EmailVerification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {
	private final JavaMailSender mailSender;

	// TODO: Redis를 사용한 인증 코드 저장소로 변경 필요
	private final Map<String, EmailVerification> verificationStore = new ConcurrentHashMap<>();

	@Override
	public String sendVerificationCode(String email) {
		try {
			// 1. 인증 코드 생성
			String verificationCode = generateVerificationCode();

			// 2. 이메일 인증 객체 생성 및 저장
			EmailVerification verification = new EmailVerification(email, verificationCode);
			verificationStore.put(email, verification);

			// 3. 이메일 발송
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			message.setSubject("[CoordieFit] 이메일 인증 코드");
			message.setText(buildEmailContent(verificationCode));

			mailSender.send(message);

			log.info("인증 코드 발송 완료: {}", email);
			return verificationCode;

		} catch (Exception e) {
			log.error("인증 코드 발송 실패: {}", email, e);
			return null;
		}
	}

	private String generateVerificationCode() {
		return String.format("%06d", new Random().nextInt(1000000));
	}

	private String buildEmailContent(String verificationCode) {
		StringBuilder content = new StringBuilder();
		content.append("안녕하세요. CoordieFit입니다.\n\n");
		content.append("회원가입을 위한 이메일 인증 코드입니다.\n\n");
		content.append("인증 코드: ").append(verificationCode).append("\n\n");
		content.append("이 코드는 10분간 유효합니다.\n");
		content.append("코드를 입력하여 이메일 인증을 완료해주세요.\n\n");
		content.append("감사합니다.");

		return content.toString();
	}

	@Override
	public boolean verifyCode(String email, String code) {
		EmailVerification verification = verificationStore.get(email);

		if (verification == null) {
			log.warn("인증 정보 없음: {}", email);
			return false;
		}

		if (verification.isExpired()) {
			log.warn("인증 코드 만료: {}", email);
			verificationStore.remove(email);
			return false;
		}

		if (verification.isValidCode(code)) {
			verification.markAsVerified();
			log.info("이메일 인증 성공: {}", email);

			// TODO: Redis사용시 변경 필요. 인증 완료 후 일정 시간 후 삭제 (여기서는 바로 삭제)
			verificationStore.remove(email);
			return true;
		}

		log.warn("인증 코드 불일치: {}", email);
		return false;
	}
}
