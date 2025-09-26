package com.miracle.coordifit.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miracle.coordifit.auth.dto.SignUpRequestDto;
import com.miracle.coordifit.auth.model.User;
import com.miracle.coordifit.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
	private final UserRepository userRepository;
	private final IEmailService emailService;

	@Override
	public User signUp(SignUpRequestDto signUpRequestDto) {
		log.info("회원가입 시작: {}", signUpRequestDto.getEmail());

		// 1. 이메일 중복 검사
		if (!isEmailAvailable(signUpRequestDto.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		// 2. 닉네임 중복 검사
		if (!isNicknameAvailable(signUpRequestDto.getNickname())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}

		// 3. 이메일 인증 코드 검증
		if (!emailService.verifyCode(signUpRequestDto.getEmail(), signUpRequestDto.getVerificationCode())) {
			throw new IllegalArgumentException("이메일 인증 코드가 올바르지 않습니다.");
		}

		// 4. 사용자 ID 생성
		String userId = generateUserId();

		// 5. 비밀번호 저장 (암호화 없이 평문 저장 - 임시)
		String password = signUpRequestDto.getPassword();

		// 6. 사용자 객체 생성
		User user = User.builder()
			.userId(userId)
			.email(signUpRequestDto.getEmail())
			.password(password)
			.nickname(signUpRequestDto.getNickname())
			.loginTypeCode("A20001") // 이메일 로그인
			.isActive("Y")
			.createdBy(userId) // 자기 자신을 생성자로 설정
			.build();

		// 7. 데이터베이스에 저장
		int result = userRepository.insertUser(user);
		if (result <= 0) {
			throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다.");
		}

		log.info("회원가입 완료: {} -> {}", signUpRequestDto.getEmail(), userId);
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isEmailAvailable(String email) {
		int count = userRepository.countByEmail(email);
		return count == 0;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isNicknameAvailable(String nickname) {
		int count = userRepository.countByNickname(nickname);
		return count == 0;
	}

	@Override
	public boolean updateLastLoginTime(String userId) {
		int result = userRepository.updateLastLoginTime(userId);
		return result > 0;
	}

	@Override
	@Transactional(readOnly = true)
	public User authenticate(String email, String password) {
		try {
			User user = userRepository.selectUserByEmail(email);

			// 평문 비밀번호 비교 (임시)
			if (user != null && password.equals(user.getPassword())) {
				// 마지막 로그인 시간 업데이트
				updateLastLoginTime(user.getUserId());
				return user;
			}

			return null; // 인증 실패

		} catch (Exception e) {
			log.error("사용자 인증 중 오류 발생", e);
			throw new RuntimeException("인증 처리 중 오류가 발생했습니다.");
		}
	}

	private String generateUserId() {
		int nextSeq = userRepository.getNextUserSequence();
		return String.format("U%06d", nextSeq);
	}
}
