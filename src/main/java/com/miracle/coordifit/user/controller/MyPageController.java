package com.miracle.coordifit.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.user.dto.MyPageResponseDto;
import com.miracle.coordifit.user.service.IMyPageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

	private final IMyPageService myPageService;

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPageInfo(
		@PathVariable String userId,
		Authentication authentication) {
		try {
			String currentUserId = authentication.getName();

			MyPageResponseDto myPageInfo = myPageService.getMyPageInfo(userId, currentUserId);

			return ResponseEntity.ok(
				ApiResponseDto.success("마이페이지 정보 조회 성공", myPageInfo));

		} catch (Exception e) {
			log.error("마이페이지 정보 조회 실패 - userId: {}", userId, e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("마이페이지 정보 조회 실패: " + e.getMessage()));
		}
	}
}
