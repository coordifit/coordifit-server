package com.miracle.coordifit.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.auth.service.IJwtService;
import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.user.dto.MyPageResponseDto;
import com.miracle.coordifit.user.dto.ProfileUpdateRequestDto;
import com.miracle.coordifit.user.dto.UserDto;
import com.miracle.coordifit.user.model.User;
import com.miracle.coordifit.user.service.IFollowService;
import com.miracle.coordifit.user.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
	private final IUserService userService;
	private final IJwtService jwtService;
	private final IFollowService followService;

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
				return ResponseEntity.ok(ApiResponseDto.success("계정 비활성 완료"));
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

	@PostMapping("/{userId}/follow")
	public ResponseEntity<ApiResponseDto<Void>> toggleFollow(
		@PathVariable String userId,
		Authentication authentication) {
		try {
			String currentUserId = authentication.getName();
			followService.toggleFollow(currentUserId, userId);

			log.info("팔로우 토글 완료: follower={}, following={}", currentUserId, userId);
			return ResponseEntity.ok(ApiResponseDto.success("팔로우 처리 완료"));
		} catch (Exception e) {
			log.error("팔로우 처리 실패: userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("팔로우 처리 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{userId}/follow")
	public ResponseEntity<ApiResponseDto<Boolean>> checkFollowing(
		@PathVariable String userId,
		Authentication authentication) {
		try {
			String currentUserId = authentication.getName();
			boolean isFollowing = followService.isFollowing(currentUserId, userId);

			log.info("팔로우 상태 조회: follower={}, following={}, isFollowing={}", currentUserId, userId, isFollowing);
			return ResponseEntity.ok(ApiResponseDto.success("팔로우 상태 조회 성공", isFollowing));
		} catch (Exception e) {
			log.error("팔로우 상태 조회 실패: userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("팔로우 상태 조회 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{userId}/followers")
	public ResponseEntity<ApiResponseDto<List<UserDto>>> getFollowers(
		@PathVariable String userId) {
		try {
			List<UserDto> followers = followService.getFollowers(userId);

			log.info("팔로워 목록 조회 완료: userId={}, count={}", userId, followers.size());
			return ResponseEntity.ok(ApiResponseDto.success("팔로워 목록 조회 성공", followers));
		} catch (Exception e) {
			log.error("팔로워 목록 조회 실패: userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("팔로워 목록 조회 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{userId}/followings")
	public ResponseEntity<ApiResponseDto<List<UserDto>>> getFollowings(
		@PathVariable String userId) {
		try {
			List<UserDto> followings = followService.getFollowings(userId);

			log.info("팔로잉 목록 조회 완료: userId={}, count={}", userId, followings.size());
			return ResponseEntity.ok(ApiResponseDto.success("팔로잉 목록 조회 성공", followings));
		} catch (Exception e) {
			log.error("팔로잉 목록 조회 실패: userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("팔로잉 목록 조회 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{userId}/mypage")
	public ResponseEntity<ApiResponseDto<MyPageResponseDto>> getMyPageInfo(
		@PathVariable String userId,
		Authentication authentication) {
		try {
			String currentUserId = authentication.getName();
			MyPageResponseDto myPageInfo = userService.getMyPageInfo(userId, currentUserId);

			log.info("마이페이지 정보 조회 완료: userId={}", userId);
			return ResponseEntity.ok(ApiResponseDto.success("마이페이지 정보 조회 성공", myPageInfo));
		} catch (Exception e) {
			log.error("마이페이지 정보 조회 실패: userId={}", userId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("마이페이지 정보 조회 실패: " + e.getMessage()));
		}
	}
}
