package com.miracle.coordifit.user.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miracle.coordifit.auth.dto.SignUpRequestDto;
import com.miracle.coordifit.auth.service.IEmailService;
import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.repository.PostRepository;
import com.miracle.coordifit.user.dto.MyPageResponseDto;
import com.miracle.coordifit.user.dto.ProfileUpdateRequestDto;
import com.miracle.coordifit.user.model.User;
import com.miracle.coordifit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final IEmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public User signUp(SignUpRequestDto signUpRequestDto) {
		log.info("회원가입 시작: {}", signUpRequestDto.getEmail());

		if (!isEmailAvailable(signUpRequestDto.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		if (!isNicknameAvailable(signUpRequestDto.getNickname())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}

		if (!emailService.verifyCode(signUpRequestDto.getEmail(), signUpRequestDto.getVerificationCode())) {
			throw new IllegalArgumentException("이메일 인증 코드가 올바르지 않습니다.");
		}

		String userId = generateUserId();
		String password = passwordEncoder.encode(signUpRequestDto.getPassword());

		User user = User.builder()
			.userId(userId)
			.email(signUpRequestDto.getEmail())
			.password(password)
			.nickname(signUpRequestDto.getNickname())
			.loginTypeCode("A20001") // 이메일 로그인
			.isActive("Y")
			.createdBy(userId)
			.build();

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

			if (user != null && passwordEncoder.matches(password, user.getPassword())) {
				updateLastLoginTime(user.getUserId());
				return user;
			}

			return null;

		} catch (Exception e) {
			log.error("사용자 인증 중 오류 발생", e);
			throw new RuntimeException("인증 처리 중 오류가 발생했습니다.");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserById(String userId) {
		try {
			return userRepository.selectUserByUserId(userId);
		} catch (Exception e) {
			log.error("사용자 조회 중 오류 발생: userId={}", userId, e);
			throw new RuntimeException("사용자 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public User updateUserProfile(String userId, ProfileUpdateRequestDto requestDto) {
		try {
			User existingUser = userRepository.selectUserByUserId(userId);
			if (existingUser == null) {
				throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
			}

			if (requestDto.getNickname() != null &&
				!requestDto.getNickname().equals(existingUser.getNickname()) &&
				!isNicknameAvailable(requestDto.getNickname())) {
				throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
			}

			User updatedUser = User.builder()
				.userId(userId)
				.email(existingUser.getEmail())
				.nickname(requestDto.getNickname() != null ? requestDto.getNickname() : existingUser.getNickname())
				.genderCode(
					requestDto.getGenderCode() != null ? requestDto.getGenderCode() : existingUser.getGenderCode())
				.birthDate(requestDto.getBirthDate() != null ? LocalDate
					.parse(requestDto.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay()
					: existingUser.getBirthDate())
				.isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : existingUser.getIsActive())
				.fileId(requestDto.getFileId() != null ? requestDto.getFileId() : existingUser.getFileId())
				.loginTypeCode(existingUser.getLoginTypeCode())
				.kakaoId(existingUser.getKakaoId())
				.updatedBy(userId)
				.build();

			int result = userRepository.updateUserProfile(updatedUser);
			if (result <= 0) {
				throw new RuntimeException("프로필 업데이트 처리 중 오류가 발생했습니다.");
			}

			return updatedUser;
		} catch (Exception e) {
			log.error("프로필 업데이트 중 오류 발생: userId={}", userId, e);
			throw e;
		}
	}

	@Override
	public void toggleUserActive(String userId) {
		try {
			User existingUser = userRepository.selectUserByUserId(userId);
			if (existingUser == null) {
				throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
			}

			User toggledUser = User.builder()
				.userId(userId)
				.isActive(existingUser.getIsActive().equals("Y") ? "N" : "Y")
				.updatedBy(userId)
				.build();

			int result = userRepository.updateIsActive(toggledUser);
			if (result <= 0) {
				throw new RuntimeException("계정 활성/비활성 처리 중 오류가 발생했습니다.");
			}

			log.info("계정 활성/비활성 완료: userId={}", userId);
		} catch (Exception e) {
			log.error("계정 활성/비활성 중 오류 발생: userId={}", userId, e);
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public MyPageResponseDto getMyPageInfo(String userId, String currentUserId) {
		MyPageResponseDto myPageInfo = userRepository.getMyPageInfo(userId);
		if (myPageInfo == null) {
			throw new RuntimeException("사용자 정보를 찾을 수 없습니다: " + userId);
		}

		List<PostDto> posts = postRepository.getUserPosts(userId);
		myPageInfo.setPostsCount(posts.size());
		myPageInfo.setPosts(posts);

		return myPageInfo;
	}

	private String generateUserId() {
		int nextSeq = userRepository.getNextUserSequence();
		return String.format("U%06d", nextSeq);
	}
}
