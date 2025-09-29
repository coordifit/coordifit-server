package com.miracle.coordifit.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.miracle.coordifit.auth.dto.*;
import com.miracle.coordifit.auth.model.User;
import com.miracle.coordifit.auth.service.IEmailService;
import com.miracle.coordifit.auth.service.IUserService;
import com.miracle.coordifit.common.dto.ApiResponseDto;

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

	@PostMapping("/signup")
	public ResponseEntity<ApiResponseDto<User>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
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
		@Valid @RequestBody EmailVerificationRequestDto requestDto, BindingResult bindingResult) {

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
	public ResponseEntity<ApiResponseDto<Boolean>> checkEmailAvailability(@RequestParam("email") String email) {

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
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> login(@RequestBody Map<String, Object> loginRequest) {

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

			// 로그인 성공 응답 데이터 준비
			Map<String, Object> responseData = new HashMap<>();
			// @formatter:off
			responseData.put("user", Map.of(
				"userId", user.getUserId(),
				"email", user.getEmail(),
				"nickname", user.getNickname())
			);
			// @formatter:on

			// TODO: JWT 토큰 생성 및 관리 구현 필요
			// - JwtService 클래스 생성
			// - Access Token, Refresh Token 발급
			// - 토큰 만료 시간 설정
			// - 토큰 검증 및 갱신 기능
			// responseData.put("token", jwtService.generateToken(user));

			return ResponseEntity.ok()
				.body(ApiResponseDto.success("로그인이 완료되었습니다.", responseData));

		} catch (Exception e) {
			log.error("로그인 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("로그인 처리 중 오류가 발생했습니다."));
		}
	}

}
