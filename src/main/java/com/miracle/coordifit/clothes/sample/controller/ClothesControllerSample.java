package com.miracle.coordifit.clothes.sample.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.clothes.sample.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.sample.dto.ClothesRequestSample;
import com.miracle.coordifit.clothes.sample.dto.ClothesResponseSample;
import com.miracle.coordifit.clothes.sample.service.IClothesServiceSample;
import com.miracle.coordifit.common.dto.ApiResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/clothes/sample")
@RequiredArgsConstructor
public class ClothesControllerSample {

	private final IClothesServiceSample clothesService;

	@PostMapping
	public ResponseEntity<ApiResponseDto<Void>> createClothes(
		ClothesRequestSample request,
		Authentication authentication) {
		try {
			String userId = authentication.getName();

			log.info("옷 등록 요청: userId={}, name={}", userId, request.getName());

			clothesService.createClothes(request, userId);

			return ResponseEntity.ok(
				ApiResponseDto.success("옷이 성공적으로 등록되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("옷 등록 실패 (잘못된 요청): {}", e.getMessage());
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(e.getMessage()));
		} catch (Exception e) {
			log.error("옷 등록 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("옷 등록 중 오류가 발생했습니다." + e.getMessage()));
		}
	}

	@PutMapping("/{clothesId}")
	public ResponseEntity<ApiResponseDto<Void>> updateClothes(
		@PathVariable String clothesId,
		ClothesRequestSample request,
		Authentication authentication) {
		try {
			String userId = authentication.getName();

			log.info("옷 수정 요청: clothesId={}, userId={}, deletedFileIds={}",
				clothesId, userId, request.getDeletedFileIds());

			clothesService.updateClothes(clothesId, request, userId);

			log.info("옷 수정 완료: clothesId={}", clothesId);
			return ResponseEntity.ok(
				ApiResponseDto.success("옷이 성공적으로 수정되었습니다.", null));
		} catch (IllegalArgumentException e) {
			log.warn("옷 수정 실패 (잘못된 요청): {}", e.getMessage());
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(e.getMessage()));
		} catch (Exception e) {
			log.error("옷 수정 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("옷 수정 중 오류가 발생했습니다." + e.getMessage()));
		}
	}

	@GetMapping
	public ResponseEntity<ApiResponseDto<List<ClothesResponseSample>>> getUserClothes(
		Authentication authentication) {
		try {
			String userId = authentication.getName();

			log.info("옷 목록 조회 요청: userId={}", userId);

			List<ClothesResponseSample> clothesList = clothesService.getUserClothes(userId);

			log.info("옷 목록 조회 완료: count={}", clothesList.size());
			return ResponseEntity.ok(
				ApiResponseDto.success("옷 목록 조회 성공", clothesList));
		} catch (Exception e) {
			log.error("옷 목록 조회 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("옷 목록 조회 중 오류가 발생했습니다."));
		}
	}

	@GetMapping("/{clothesId}")
	public ResponseEntity<ApiResponseDto<ClothesDetailResponseDto>> getClothesDetail(
		@PathVariable String clothesId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();

			log.info("옷 상세 조회 요청: clothesId={}, userId={}", clothesId, userId);

			ClothesDetailResponseDto clothes = clothesService.getClothesDetail(clothesId, userId);

			log.info("옷 상세 조회 완료: clothesId={}", clothesId);
			return ResponseEntity.ok(
				ApiResponseDto.success("옷 상세 조회 성공", clothes));
		} catch (IllegalArgumentException e) {
			log.warn("옷 상세 조회 실패 (잘못된 요청): {}", e.getMessage());
			return ResponseEntity.badRequest()
				.body(ApiResponseDto.error(e.getMessage()));
		} catch (Exception e) {
			log.error("옷 상세 조회 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("옷 상세 조회 중 오류가 발생했습니다."));
		}
	}
}
