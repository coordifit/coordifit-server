package com.miracle.coordifit.nanobanana.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.miracle.coordifit.nanobanana.dto.ImageGenerationRequestDTO;
import com.miracle.coordifit.nanobanana.dto.ImageGenerationResponseDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Google Gemini API 호출 서비스
 * - URL 또는 Base64 이미지를 포함한 ImageGenerationRequestDTO 기반 요청
 */
@Service
@RequiredArgsConstructor
public class NanobananaService implements INanobananaService {

	private final WebClient webClient;

	@Value("${app.google.gemini.api-key}")
	private String apiKey;

	/**
	 * Gemini 이미지 생성 API 호출
	 * @param requestDTO - ImageGenerationRequestDTO (이미 URL → Base64로 변환 완료)
	 * @return Mono<String> (Base64 인코딩된 이미지 데이터)
	 */
	@Override
	public Mono<String> generateImage(ImageGenerationRequestDTO requestDTO) {
		return webClient.post()
			.uri(uriBuilder -> uriBuilder
				.path("/v1beta/models/gemini-2.5-flash-image-preview:generateContent")
				.queryParam("key", apiKey)
				.build())
			.bodyValue(requestDTO)
			.retrieve()
			.bodyToMono(ImageGenerationResponseDTO.class)
			.map(response -> {
				// ✅ Gemini 응답 파싱
				if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
					throw new RuntimeException("No candidates returned from Gemini API.");
				}

				var candidate = response.getCandidates().get(0);
				var content = candidate.getContent();
				if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
					throw new RuntimeException("Gemini response content parts are empty.");
				}

				var part = content.getParts().get(0);

				// ✅ Base64 이미지 추출
				if (part.getInlineData() != null && part.getInlineData().getData() != null) {
					return part.getInlineData().getData();
				}
				// ✅ 텍스트 fallback
				else if (part.getText() != null) {
					return "[TEXT RESPONSE] " + part.getText();
				} else {
					throw new RuntimeException("Gemini response has no usable content.");
				}
			})
			.doOnNext(data -> System.out.println("✅ Gemini Image generation success (base64 length): " + data.length()))
			.doOnError(e -> System.err.println("❌ Error calling Gemini API: " + e.getMessage()));
	}
}
