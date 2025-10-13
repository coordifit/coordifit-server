package com.miracle.coordifit.nanobanana.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miracle.coordifit.nanobanana.dto.FittingRequestDTO;
import com.miracle.coordifit.nanobanana.dto.ImageGenerationRequestDTO;
import com.miracle.coordifit.nanobanana.service.INanobananaService;
import com.miracle.coordifit.nanobanana.util.ImageUtil;

import lombok.RequiredArgsConstructor;

/**
 * 아바타 + 의류 기반 가상 피팅 API 컨트롤러 (MVC 버전)
 * 프론트는 URL만 전달하면 됨. 서버에서 Base64로 변환 후 Gemini로 요청.
 */
@RestController
@RequestMapping("/api/nanobanana")
@RequiredArgsConstructor
public class NanobananaController {

	private final INanobananaService nanobananaService;

	/**
	 * 아바타 + 의류 URL 기반 가상 피팅 요청
	 * 요청 JSON 예시:
	 * {
	 *   "avatarImage": "https://s3.../avatar.jpg",
	 *   "topImage": "https://s3.../top.png",
	 *   "bottomImage": "https://s3.../bottom.png",
	 *   "shoesImage": "https://s3.../shoes.png"
	 * }
	 */
	@PostMapping("/fitting")
	public ResponseEntity<Map<String, Object>> fitting(@RequestBody FittingRequestDTO request) {
		try {
			ImageGenerationRequestDTO geminiRequest = buildGeminiRequest(request);

			long start = System.currentTimeMillis();
			String base64 = nanobananaService.generateImageSync(geminiRequest); // ✅ 동기식 서비스 메서드 호출
			long duration = System.currentTimeMillis() - start;

			Map<String, Object> response = Map.of(
				"status", "success",
				"data", Map.of(
					"imageBase64", base64,
					"durationMs", duration));
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of(
				"status", "error",
				"message", "Fitting failed: " + e.getMessage()));
		}
	}

	/* ------------------------------------------------------------------
	 * 내부 헬퍼 메서드
	 * ------------------------------------------------------------------ */

	/**
	 * URL 기반 요청 → Gemini용 DTO 변환
	 */
	private ImageGenerationRequestDTO buildGeminiRequest(FittingRequestDTO request) throws Exception {
		List<ImageGenerationRequestDTO.Part> parts = new ArrayList<>();

		// (1) 설명 프롬프트 추가
		parts.add(new ImageGenerationRequestDTO.Part(
			"Generate a realistic virtual fitting image of the avatar wearing the provided clothes.\n" +
				"The output should be vertically oriented (portrait), approximately 220x240 pixels in size.\n" +
				"Ensure the person is centered in the frame with minimal background.\n" +
				"Maintain realistic lighting, proportions, and seamless clothing alignment.",
			null));

		// (2) 이미지 데이터 추가
		if (request.getAvatarImage() != null)
			parts.add(new ImageGenerationRequestDTO.Part(null, ImageUtil.urlToInlineData(request.getAvatarImage())));
		if (request.getTopImage() != null)
			parts.add(new ImageGenerationRequestDTO.Part(null, ImageUtil.urlToInlineData(request.getTopImage())));
		if (request.getBottomImage() != null)
			parts.add(new ImageGenerationRequestDTO.Part(null, ImageUtil.urlToInlineData(request.getBottomImage())));
		if (request.getShoesImage() != null)
			parts.add(new ImageGenerationRequestDTO.Part(null, ImageUtil.urlToInlineData(request.getShoesImage())));

		ImageGenerationRequestDTO dto = new ImageGenerationRequestDTO();
		dto.setContents(List.of(new ImageGenerationRequestDTO.Contents(parts)));
		dto.setGenerationConfig(new ImageGenerationRequestDTO.GenerationConfig(List.of("image")));
		return dto;
	}
}
