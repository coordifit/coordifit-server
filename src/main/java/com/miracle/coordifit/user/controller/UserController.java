package com.miracle.coordifit.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.user.dto.ProfileUpdateRequestDto;
import com.miracle.coordifit.user.service.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
	private final IUserService userService;

	@PutMapping("/{userId}")
	public ResponseEntity<String> updateProfile(
		@PathVariable("userId") String userId,
		@RequestBody ProfileUpdateRequestDto requestDto) {
		try {
			userService.updateUserProfile(userId, requestDto);
			return ResponseEntity.ok("프로필이 성공적으로 업데이트되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("프로필 업데이트 실패: " + e.getMessage());
		}
	}
}
