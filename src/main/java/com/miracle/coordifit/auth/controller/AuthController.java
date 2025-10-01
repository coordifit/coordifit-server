package com.miracle.coordifit.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.miracle.coordifit.auth.dto.*;
import com.miracle.coordifit.auth.repository.JwtTokenRepository;
import com.miracle.coordifit.auth.service.IEmailService;
import com.miracle.coordifit.auth.service.IJwtService;
import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.user.model.User;
import com.miracle.coordifit.user.service.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final IUserService userService;
	private final IEmailService emailService;
	private final IJwtService jwtService;
	private final JwtTokenRepository jwtTokenRepository;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponseDto<User>> signUp(
		@Valid @RequestBody SignUpRequestDto signUpRequestDto,
		BindingResult bindingResult) {

		log.info("회원가입 요청: {}", signUpRequestDto.getEmail());

		// 유효성 검사 오류 확인
		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(errorMessage));
		}

		try {
			User user = userService.signUp(signUpRequestDto);

			// 비밀번호 정보는 응답에서 제거
			user.setPassword(null);

			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponseDto.success("회원가입이 완료되었습니다.", user));

		} catch (IllegalArgumentException e) {
			log.warn("회원가입 실패 - 유효성 오류: {}", e.getMessage());
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(e.getMessage()));

		} catch (Exception e) {
			log.error("회원가입 실패 - 시스템 오류", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("회원가입 처리 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/send-verification")
	public ResponseEntity<ApiResponseDto<String>> sendVerificationCode(
		@Valid @RequestBody EmailVerificationRequestDto requestDto,
		BindingResult bindingResult) {

		log.info("이메일 인증 코드 발송 요청: {}", requestDto.getEmail());

		// 유효성 검사 오류 확인
		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(errorMessage));
		}

		try {
			// 이메일 중복 검사
			if (!userService.isEmailAvailable(requestDto.getEmail())) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("이미 사용 중인 이메일입니다."));
			}
			String verificationCode = emailService.sendVerificationCode(requestDto.getEmail());

			if (verificationCode != null) {
				return ResponseEntity.ok()
					.body(ApiResponseDto.success("인증 코드가 발송되었습니다.", verificationCode));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponseDto.error("인증 코드 발송에 실패했습니다."));
			}

		} catch (Exception e) {
			log.error("인증 코드 발송 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("인증 코드 발송 중 오류가 발생했습니다."));
		}
	}

	@GetMapping("/check-email")
	public ResponseEntity<ApiResponseDto<Boolean>> checkEmailAvailability(
		@RequestParam("email") String email) {

		log.info("이메일 중복 검사: {}", email);

		try {
			boolean available = userService.isEmailAvailable(email);
			String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

			return ResponseEntity.ok()
				.body(ApiResponseDto.success(message, available));

		} catch (Exception e) {
			log.error("이메일 중복 검사 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("이메일 중복 검사 중 오류가 발생했습니다."));
		}
	}

	@GetMapping("/check-nickname")
	public ResponseEntity<ApiResponseDto<Boolean>> checkNicknameAvailability(
		@RequestParam("nickname") String nickname) {

		log.info("닉네임 중복 검사: {}", nickname);

		try {
			boolean available = userService.isNicknameAvailable(nickname);
			String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";

			return ResponseEntity.ok()
				.body(ApiResponseDto.success(message, available));

		} catch (Exception e) {
			log.error("닉네임 중복 검사 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("닉네임 중복 검사 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> login(
		@RequestBody Map<String, Object> loginRequest) {

		log.info("로그인 요청: {}", loginRequest.get("email"));

		try {
			String email = (String)loginRequest.get("email");
			String password = (String)loginRequest.get("password");

			// 입력 값 검증
			if (email == null || email.trim().isEmpty()) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("이메일을 입력해주세요."));
			}

			if (password == null || password.trim().isEmpty()) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("비밀번호를 입력해주세요."));
			}

			// 사용자 인증 (userService에 메소드 추가 필요)
			User user = userService.authenticate(email, password);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("이메일 또는 비밀번호가 올바르지 않습니다."));
			}

			// JWT 토큰 생성
			String accessToken = jwtService.generateToken(user, "ACCESS");
			String refreshToken = jwtService.generateToken(user, "REFRESH");

			// 기존 토큰 삭제 (보안을 위해)
			jwtTokenRepository.deleteToken(user.getUserId(), "access");
			jwtTokenRepository.deleteToken(user.getUserId(), "refresh");

			// 새 토큰들을 Redis에 저장
			jwtTokenRepository.saveToken(user.getUserId(), accessToken, jwtService.getExpirationFromToken(accessToken),
				"access");
			jwtTokenRepository.saveToken(user.getUserId(), refreshToken,
				jwtService.getExpirationFromToken(refreshToken), "refresh");

			// 로그인 성공 응답 데이터 준비 (중복 사용자 정보 제거: JWT에 포함됨)
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("accessToken", accessToken);
			responseData.put("refreshToken", refreshToken);
			responseData.put("tokenType", "Bearer");

			return ResponseEntity.ok()
				.body(ApiResponseDto.success("로그인이 완료되었습니다.", responseData));

		} catch (Exception e) {
			log.error("로그인 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("로그인 처리 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> refreshToken(
		@RequestBody Map<String, String> refreshRequest) {

		log.info("토큰 갱신 요청");

		try {
			String refreshToken = refreshRequest.get("refreshToken");

			if (refreshToken == null || refreshToken.trim().isEmpty()) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("리프레시 토큰을 입력해주세요."));
			}

			// 리프레시 토큰 검증
			if (!jwtService.validateToken(refreshToken)) {
				log.warn("리프레시 토큰 JWT 검증 실패");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("유효하지 않은 리프레시 토큰입니다."));
			}

			log.info("리프레시 토큰 검증 성공");

			// 사용자 정보 조회
			String userId = jwtService.getUserIdFromToken(refreshToken);
			User user = userService.getUserById(userId);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("사용자를 찾을 수 없습니다."));
			}

			// 새 액세스 토큰 생성
			String newAccessToken = jwtService.generateToken(user, "ACCESS");

			// 기존 액세스 토큰 삭제
			jwtTokenRepository.deleteToken(userId, "access");

			// 새 액세스 토큰 저장
			jwtTokenRepository.saveToken(userId, newAccessToken, jwtService.getExpirationFromToken(newAccessToken),
				"access");

			// 응답 데이터 준비
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("accessToken", newAccessToken);
			responseData.put("tokenType", "Bearer");

			return ResponseEntity.ok()
				.body(ApiResponseDto.success("토큰이 갱신되었습니다.", responseData));

		} catch (Exception e) {
			log.error("토큰 갱신 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("토큰 갱신 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponseDto<String>> logout(
		@RequestHeader("Authorization") String authorization) {

		log.info("로그아웃 요청");

		try {
			// JWT 필터에서 이미 검증됨 - 토큰에서 사용자 ID 추출
			String accessToken = authorization.substring(7);
			String userId = jwtService.getUserIdFromToken(accessToken);

			// 해당 사용자의 모든 토큰 삭제
			jwtTokenRepository.deleteToken(userId, "access");
			jwtTokenRepository.deleteToken(userId, "refresh");

			log.info("로그아웃 완료: {}", userId);

			return ResponseEntity.ok()
				.body(ApiResponseDto.success("로그아웃이 완료되었습니다.", null));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("로그아웃 처리 중 오류가 발생했습니다."));
		}
	}
}
