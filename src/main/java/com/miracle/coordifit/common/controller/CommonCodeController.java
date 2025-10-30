package com.miracle.coordifit.common.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.common.dto.CategoryResponseDto;
import com.miracle.coordifit.common.model.CommonCode;
import com.miracle.coordifit.common.service.ICommonCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {
	private final ICommonCodeService commonCodeService;

	@GetMapping
	public ResponseEntity<ApiResponseDto<Map<String, CommonCode>>> getCommonCodes() {
		try {
			Map<String, CommonCode> commonCodes = commonCodeService.getCommonCodes();
			return ResponseEntity.ok(ApiResponseDto.success("공통 코드 조회 성공", commonCodes));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("공통 코드 조회 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{parentCodeId}")
	public ResponseEntity<ApiResponseDto<List<CommonCode>>> getCommonCodesByParentCodeId(
		@PathVariable("parentCodeId") String parentCodeId) {
		try {
			List<CommonCode> commonCodes = commonCodeService.getCommonCodesByParentCodeId(parentCodeId);
			return ResponseEntity.ok(ApiResponseDto.success("공통 코드 조회 성공", commonCodes));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("공통 코드 조회 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/category")
	public ResponseEntity<ApiResponseDto<CategoryResponseDto>> getCategoryData() {
		try {
			CategoryResponseDto categoryData = commonCodeService.getCategoryData();
			return ResponseEntity.ok(ApiResponseDto.success("카테고리 데이터 조회 성공", categoryData));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("카테고리 데이터 조회 실패: " + e.getMessage()));
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponseDto<Void>> createCommonCode(
		@RequestBody CommonCode commonCode,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			commonCodeService.createCommonCode(commonCode, userId);
			return ResponseEntity.ok(ApiResponseDto.success("공통 코드 생성 성공"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("공통 코드 생성 실패: " + e.getMessage()));
		}
	}

	@PutMapping("/{codeId}")
	public ResponseEntity<ApiResponseDto<Void>> updateCommonCode(
		@PathVariable("codeId") String codeId,
		@RequestBody CommonCode commonCode,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			commonCodeService.updateCommonCode(commonCode, codeId, userId);
			return ResponseEntity.ok(ApiResponseDto.success("공통 코드 수정 성공"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("공통 코드 수정 실패: " + e.getMessage()));
		}
	}

	@DeleteMapping("/{codeId}")
	public ResponseEntity<ApiResponseDto<Void>> deleteCommonCode(@PathVariable("codeId") String codeId) {
		try {
			commonCodeService.deleteCommonCode(codeId);
			return ResponseEntity.ok(ApiResponseDto.success("공통 코드 삭제 성공"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("공통 코드 삭제 실패: " + e.getMessage()));
		}
	}
}
