// src/main/java/com/miracle/coordifit/clothes/controller/ClothesController.java
package com.miracle.coordifit.clothes.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.miracle.coordifit.clothes.dto.*;
import com.miracle.coordifit.clothes.repository.ClothesRepository;
import com.miracle.coordifit.clothes.service.IClothesService;
import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.common.model.CommonCode;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.ICommonCodeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Clothes", description = "옷 등록/수정/조회 API (Base64 이미지)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

	private final IClothesService clothesService;
	private final ICommonCodeService commonCodeService;
	private final ClothesRepository clothesRepository;

	@Operation(summary = "등록/수정 폼 데이터")
	@GetMapping("/form")
	public ApiResponseDto<Map<String, Object>> form() {
		Map<String, Object> res = new HashMap<>();
		Map<String, CommonCode> all = commonCodeService.getCommonCodes();
		res.put("categories", all.get("B10001"));
		res.put("uploadPolicy", Map.of("min", 1, "max", 5, "maxSizeMB", 10));
		return ApiResponseDto.success("OK", res);
	}

	// ================== Base64 단건 등록 ==================
	@Operation(summary = "옷 등록 (Base64 이미지)")
	@PostMapping(path = "/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseDto<Map<String, Object>> createOne(
		@Valid @RequestBody ClothesCreateWithImagesRequest req,
		@RequestHeader("X-Actor") String actor) {
		try {
			// 🔎 사전검증: 어디서 깨지는지 imageIndex로 즉시 표시
			int j = 0;
			for (var img : req.getImages()) {
				j++;
				// FileService의 프리플라이트 디코더 호출 (로그에 head/tail/len/mod 찍힘)
				com.miracle.coordifit.common.service.FileService.decodeBase64SafeForPreflight(img.getDataUrl());
			}

			validateCategoryCode(req.getCategoryCode());
			String id = clothesService.createOneBase64(req, actor);
			return ApiResponseDto.success("등록 성공", Map.of("clothesId", id));
		} catch (Exception e) {
			return ApiResponseDto.error("등록 실패: " + root(e));
		}
	}

	// ================== Base64 벌크 등록(병렬) ==================
	@Operation(summary = "옷 일괄 등록 (Base64, 병렬)")
	@PostMapping(path = "/base64/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseDto<List<String>> bulkCreate(
		@Valid @RequestBody ClothesBulkCreateWithImagesRequest req,
		@RequestHeader("X-Actor") String actor) {
		try {
			if (req.getItems() != null && !req.getItems().isEmpty()) {
				validateCategoryCodes(
					req.getItems().stream().map(ClothesCreateWithImagesRequest::getCategoryCode)
						.collect(Collectors.toList()));
			}
			return ApiResponseDto.success("일괄 등록 성공",
				clothesService.bulkCreateBase64Parallel(req, actor));
		} catch (Exception e) {
			return ApiResponseDto.error("일괄 등록 실패: " + root(e));
		}
	}

	// ================== Base64 수정 ==================
	@Operation(summary = "옷 수정 (Base64 이미지)")
	@PutMapping(path = "/{clothesId}/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseDto<Void> update(
		@PathVariable String clothesId,
		@Valid @RequestBody ClothesUpdateWithImagesRequest req,
		@RequestHeader("X-Actor") String actor) {
		try {
			if (req.getCategoryCode() != null)
				validateCategoryCode(req.getCategoryCode());
			clothesService.updateBase64(clothesId, req, actor);
			return ApiResponseDto.success("수정 성공");
		} catch (Exception e) {
			return ApiResponseDto.error("수정 실패: " + root(e));
		}
	}

	// ================== 이미지 개별 삭제 ==================
	@Operation(summary = "이미지 개별 삭제")
	@DeleteMapping("/{clothesId}/images/{fileId}")
	public ApiResponseDto<Void> deleteImage(@PathVariable String clothesId, @PathVariable Long fileId) {
		try {
			clothesService.removeImage(clothesId, fileId);
			return ApiResponseDto.success("이미지 삭제 성공");
		} catch (Exception e) {
			return ApiResponseDto.error("이미지 삭제 실패: " + root(e));
		}
	}

	// ================== 삭제/벌크 삭제 ==================
	@Operation(summary = "옷 삭제(소프트삭제)")
	@DeleteMapping("/{clothesId}")
	public ApiResponseDto<Void> remove(@PathVariable String clothesId) {
		try {
			clothesService.remove(clothesId);
			return ApiResponseDto.success("삭제 성공");
		} catch (Exception e) {
			return ApiResponseDto.error("삭제 실패: " + root(e));
		}
	}

	@Operation(summary = "옷 일괄 삭제")
	@DeleteMapping("/bulk")
	public ApiResponseDto<Void> bulkDelete(@RequestBody List<String> clothesIds) {
		try {
			clothesService.bulkDelete(clothesIds);
			return ApiResponseDto.success("일괄 삭제 성공");
		} catch (Exception e) {
			return ApiResponseDto.error("일괄 삭제 실패: " + root(e));
		}
	}

	// ================== 조회 ==================
	@Operation(summary = "옷 상세 조회 (images[] + thumbnailUrl 포함)")
	@GetMapping("/{clothesId}")
	public ApiResponseDto<ClothesDetailDto> detail(@PathVariable String clothesId) {
		try {
			return ApiResponseDto.success("조회 성공", clothesService.findDetail(clothesId));
		} catch (Exception e) {
			return ApiResponseDto.error("조회 실패: " + root(e));
		}
	}

	@Operation(summary = "내 옷 전체(썸네일 목록)")
	@GetMapping("/me")
	public ApiResponseDto<List<ClothesListItemDto>> myClothes(@RequestHeader("X-User-Id") String userId) {
		return ApiResponseDto.success("OK", clothesService.findAllByUser(userId));
	}

	@Operation(summary = "옷 이미지 목록 (파일 상세)")
	@GetMapping("/{clothesId}/images")
	public ApiResponseDto<List<FileInfo>> images(@PathVariable String clothesId) {
		return ApiResponseDto.success("OK", clothesService.findImages(clothesId));
	}

	@Operation(summary = "옷 전체 조회(정렬/필터/페이징)")
	@GetMapping
	public ApiResponseDto<Map<String, Object>> getClothes(
		@RequestParam(required = false) String categoryCode,
		@RequestParam(defaultValue = "purchaseDate") String sort,
		@RequestParam(defaultValue = "desc") String dir,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {

		List<ClothesDetailDto> all = clothesService.getClothes(categoryCode, null);

		Comparator<ClothesDetailDto> comp;
		if ("price".equalsIgnoreCase(sort)) {
			comp = Comparator.comparing(dto -> dto.getPrice() == null ? Integer.MIN_VALUE : dto.getPrice());
		} else if ("name".equalsIgnoreCase(sort)) {
			comp = Comparator.comparing(dto -> dto.getName() == null ? "" : dto.getName(),
				String.CASE_INSENSITIVE_ORDER);
		} else {
			comp = Comparator.comparing(ClothesDetailDto::getPurchaseDate,
				Comparator.nullsLast(Comparator.naturalOrder()));
		}
		if ("desc".equalsIgnoreCase(dir))
			comp = comp.reversed();

		List<ClothesDetailDto> sorted = all.stream().sorted(comp).toList();
		int safeSize = Math.max(size, 1);
		int from = Math.max(page, 0) * safeSize;
		int to = Math.min(from + safeSize, sorted.size());
		List<ClothesDetailDto> content = from >= to ? List.of() : sorted.subList(from, to);

		Map<String, Object> body = new HashMap<>();
		body.put("content", content);
		body.put("totalElements", sorted.size());
		body.put("page", page);
		body.put("size", safeSize);
		return ApiResponseDto.success("OK", body);
	}

	// ================== 유틸 ==================
	private void validateCategoryCode(String categoryCode) {
		if (categoryCode == null)
			return;
		String code = categoryCode.trim();
		if (code.isEmpty())
			throw new IllegalArgumentException("유효하지 않은 categoryCode(공백)");
		int n = clothesRepository.existsActiveCategoryCount(code);
		if (n == 0)
			throw new IllegalArgumentException("유효하지 않은 categoryCode: " + code);
	}

	private void validateCategoryCodes(List<String> codes) {
		if (codes == null)
			return;
		for (String c : codes) {
			if (c == null)
				continue;
			String code = c.trim();
			if (code.isEmpty())
				throw new IllegalArgumentException("유효하지 않은 categoryCode(공백)");
			int n = clothesRepository.existsActiveCategoryCount(code);
			if (n == 0)
				throw new IllegalArgumentException("유효하지 않은 categoryCode: " + code);
		}
	}

	private String root(Throwable t) {
		Throwable r = t;
		while (r.getCause() != null)
			r = r.getCause();
		return r.getClass().getSimpleName() + ": " + String.valueOf(r.getMessage());
	}
}
