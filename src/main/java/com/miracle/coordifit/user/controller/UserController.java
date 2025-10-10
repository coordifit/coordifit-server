package com.miracle.coordifit.user.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.auth.service.IJwtService;
import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.user.dto.ProfileUpdateRequestDto;
import com.miracle.coordifit.user.model.User;
import com.miracle.coordifit.user.service.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
	private final IUserService userService;
	private final IJwtService jwtService;

	@PutMapping("/{userId}")
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateProfile(
		@PathVariable("userId") String userId,
		@RequestBody ProfileUpdateRequestDto requestDto) {
		try {
			User updatedUser = userService.updateUserProfile(userId, requestDto);

			Map<String, Object> tokenData = jwtService.createTokens(updatedUser);

			return ResponseEntity.ok(
				ApiResponseDto.success("프로필이 성공적으로 업데이트되었습니다.", tokenData));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("프로필 업데이트 실패: " + e.getMessage()));
		}
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> toggleUserActive(
		@PathVariable("userId") String userId,
		@RequestParam(value = "isActive", defaultValue = "N") String isActive) {

		try {
			userService.toggleUserActive(userId);
			if ("Y".equals(isActive)) {
				Map<String, Object> tokenData = activateUser(userId);
				return ResponseEntity.ok(ApiResponseDto.success("계정 활성 완료", tokenData));
			} else {
				deactivateUser(userId);
				return ResponseEntity.ok(ApiResponseDto.success("계정 비활성 완료", null));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("계정 활성/비활성 실패: " + e.getMessage()));
		}
	}

	private void deactivateUser(String userId) {
		try {
			jwtService.deleteAllUserTokens(userId);
		} catch (Exception e) {
			throw new RuntimeException("계정 삭제 실패: " + e.getMessage());
		}
	}

	private Map<String, Object> activateUser(String userId) {
		try {
			User user = userService.getUserById(userId);
			if (user == null) {
				throw new RuntimeException("사용자를 찾을 수 없습니다.");
			}

			Map<String, Object> responseData = jwtService.createTokens(user);
			return responseData;

		} catch (Exception e) {
			throw new RuntimeException("계정 활성화 실패: " + e.getMessage());
		}
	}
}
