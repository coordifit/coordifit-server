package com.miracle.coordifit.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.auth.dto.EmailVerificationRequestDto;
import com.miracle.coordifit.auth.dto.SignUpRequestDto;
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

	@PostMapping("/signup")
	public ResponseEntity<ApiResponseDto<User>> signUp(
		@Valid @RequestBody SignUpRequestDto signUpRequestDto,
		BindingResult bindingResult) {

		log.info("회원가입 요청: {}", signUpRequestDto.getEmail());

		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(errorMessage));
		}

		try {
			User user = userService.signUp(signUpRequestDto);
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

		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(errorMessage));
		}

		try {
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

			if (email == null || email.trim().isEmpty()) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("이메일을 입력해주세요."));
			}

			if (password == null || password.trim().isEmpty()) {
				return ResponseEntity.badRequest()
					.body(ApiResponseDto.error("비밀번호를 입력해주세요."));
			}

			User user = userService.authenticate(email, password);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("이메일 또는 비밀번호가 올바르지 않습니다."));
			}

			if ("N".equals(user.getIsActive())) {
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("isActive", false);
				responseData.put("message", "비활성화된 계정입니다. 계정을 다시 활성화하시겠습니까?");
				responseData.put("userId", user.getUserId());

				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponseDto.error("비활성화된 계정입니다.", responseData));
			}

			Map<String, Object> responseData = jwtService.createTokens(user);

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

			if (!jwtService.validateToken(refreshToken)) {
				log.warn("리프레시 토큰 JWT 검증 실패");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("유효하지 않은 리프레시 토큰입니다."));
			}

			log.info("리프레시 토큰 검증 성공");

			String userId = jwtService.getUserIdFromToken(refreshToken);
			User user = userService.getUserById(userId);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseDto.error("사용자를 찾을 수 없습니다."));
			}

			Map<String, Object> responseData = jwtService.refreshAccessToken(user);

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
			String accessToken = authorization.substring(7);
			String userId = jwtService.getUserIdFromToken(accessToken);

			jwtService.deleteAllUserTokens(userId);

			log.info("로그아웃 완료: {}", userId);

			return ResponseEntity.ok()
				.body(ApiResponseDto.success("로그아웃이 완료되었습니다.", null));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("로그아웃 처리 중 오류가 발생했습니다."));
		}
	}
}
