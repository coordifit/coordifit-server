package com.miracle.coordifit.coordi.controller;

import java.net.URI;
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
import org.springframework.web.bind.annotation.PutMapping;
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

import jakarta.validation.constraints.NotBlank;
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
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponseDto<?>> createCoordi(
		Authentication authentication,
		@RequestPart("image") MultipartFile image,
		@RequestParam("canvasJson") @NotBlank String canvasJson,
		@RequestParam("coordiName") @NotBlank String coordiName,
		@RequestParam(value = "description", required = false) String description) {
		final String userId = (String)authentication.getPrincipal();
		try {
			log.info(">> POST /api/coordi - userId={}", userId);

			FileInfo imageInfo = fileService.uploadFile(image);

			Coordi coordi = Coordi.builder()
				.userId(userId)
				.coordiName(coordiName)
				.fileId(imageInfo.getFileId())
				.description(description)
				.canvasJson(canvasJson)
				.build();

			int affected = coordiService.upsertCoordi(coordi);

			URI location = URI.create("/api/coordi/" + coordi.getCoordiId());

			Map<String, Object> body = Map.of("coordiId", coordi.getCoordiId(), "affectedRows", affected);

			return ResponseEntity.created(location).body(ApiResponseDto.success("코디 등록 성공", body));

		} catch (Exception e) {
			log.error(">> createCoordi failed", e);
			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("코디 저장 실패", e.getMessage()));
		}
	}

	@Transactional
	@PutMapping(value = "/{coordiId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponseDto<?>> updateCoordi(
		Authentication authentication,
		@PathVariable("coordiId") @NotBlank String coordiId,
		@RequestPart(value = "image") MultipartFile image,
		@RequestParam("canvasJson") @NotBlank String canvasJson,
		@RequestParam("coordiName") @NotBlank String coordiName,
		@RequestParam(value = "description") String description) {
		final String userId = (String)authentication.getPrincipal();

		try {
			log.info(">> PUT /api/coordi/{} - userId={}", coordiId, userId);

			FileInfo originImage = fileService.uploadFile(image);
			int fileId = originImage.getFileId();

			Coordi coordi = Coordi.builder()
				.coordiId(coordiId)
				.userId(userId)
				.coordiName(coordiName)
				.fileId(fileId)
				.description(description)
				.canvasJson(canvasJson)
				.build();

			int affected = coordiService.upsertCoordi(coordi);

			Map<String, Object> body = Map.of(
				"coordiId", coordiId,
				"affectedRows", affected);
			return ResponseEntity.ok(ApiResponseDto.success("코디 수정 성공", body));

		} catch (Exception e) {
			log.error(">> updateCoordi failed", e);
			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("코디 수정 실패", e.getMessage()));
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
