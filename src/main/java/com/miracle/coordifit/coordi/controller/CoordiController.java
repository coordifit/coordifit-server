package com.miracle.coordifit.coordi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;
import com.miracle.coordifit.coordi.dto.CoordiResponse;
import com.miracle.coordifit.coordi.model.Coordi;
import com.miracle.coordifit.coordi.service.ICoordiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/coordi")
@RequiredArgsConstructor
@Slf4j
public class CoordiController {
	private final ICoordiService coordiService;
	private final IFileService fileService;

	@GetMapping
	public ResponseEntity<ApiResponseDto<?>> getAllCoordis(Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		log.info(">> get user ID Whern getAllCoordis {}", userId);

		try {
			log.info(">> GET /api/coordi - getAllCoordis for userId={}", userId);
			List<CoordiResponse> coordiList = coordiService.getAllCoordisByUser(userId);

			return ResponseEntity.ok(ApiResponseDto.success("코디가 성공적으로 조회되었습니다.", coordiList));

		} catch (IllegalArgumentException e) {
			log.error("코디 조회 실패(잘못된 요청) : {}", e.getMessage());

			return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
		} catch (Exception e) {
			log.error("코디 조회 실패: {}", e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("코디 조회중 오류가 발생했습니다." + e.getMessage()));
		}
	};

	@GetMapping("/{coordiId}")
	public ResponseEntity<ApiResponseDto<?>> getCoordById(
		@PathVariable String coordiId,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		try {
			log.info(">> GET /api/coordi/{} - getCoordById for userId={}", coordiId, userId);
			CoordiResponse coordi = coordiService.getCoordiById(coordiId);

			return ResponseEntity.ok(ApiResponseDto.success("코디 조회 성공", coordi));
		} catch (Exception e) {
			log.error(">> getCoordById failed", e);

			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("코디 조회 실패", e.getMessage()));
		}
	}

	@Transactional
	@PostMapping(value = {"", "/{coordiId}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponseDto<?>> createOrUpdateCoordi(
		Authentication authentication,
		@PathVariable(required = false) String coordiId,
		@RequestPart("image") MultipartFile image,
		@RequestParam("canvasJson") String canvasJson,
		@RequestParam("title") String title,
		@RequestParam("description") String description) {
		String userId = (String)authentication.getPrincipal();
		try {
			log.info(">> POST /api/coordi - userId={}", userId);

			FileInfo originImage = fileService.uploadFile(image);
			log.info(">> upload original Image from createCoordi");

			FileInfo thumbImage = fileService.uploadThumbnail(image);
			log.info(">> upload thumbnails Image from createCoordi");

			Coordi coordi = Coordi.builder()
				.userId(userId)
				.title(title)
				.originImageId(originImage.getFileId())
				.thumbImageId(thumbImage.getFileId())
				.description(description)
				.canvasJson(canvasJson)
				.build();

			if (coordiId != null && !coordiId.isBlank()) {
				coordi.setCoordiId(coordiId);
			}

			// 3️⃣ Upsert
			int result = coordiService.upsertCoordi(coordi);

			Map<String, Object> response = Map.of(
				"coordiId", coordi.getCoordiId(),
				"affectedRows", result);

			return ResponseEntity.ok(ApiResponseDto.success(
				"코디 등록 / 수정 성공", response));
		} catch (Exception e) {
			log.error(">> create Or Update Coordi failed", e);

			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("코디 저장 실패", e.getMessage()));
		}
	}

	@Transactional
	@DeleteMapping("/{coordiId}")
	public ResponseEntity<ApiResponseDto<?>> deleteCoordi(
		@PathVariable String coordiId,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();
		try {
			log.info(">> DELETE /api/coordi/{} - userId={}", coordiId, userId);
			coordiService.deleteCoordi(coordiId);
			return ResponseEntity.ok(ApiResponseDto.success("코디 삭제 성공", coordiId));
		} catch (Exception e) {
			log.error(">> deleteCoordi failed", e);
			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("코디 삭제 실패", e.getMessage()));
		}
	}
}
