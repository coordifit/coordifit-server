package com.miracle.coordifit.nanobanana.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.miracle.coordifit.nanobanana.dto.ImageGenerationRequestDTO;
import com.miracle.coordifit.nanobanana.dto.ImageGenerationResponseDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NanobananaService implements INanobananaService {

	private final WebClient webClient;

	@Value("${app.google.gemini.api-key}")
	private String apiKey;

	@Override
	public Mono<String> generateImage(String prompt) {
		// API 요청 페이로드 생성 (IMAGE만 요청)
		ImageGenerationRequestDTO request = new ImageGenerationRequestDTO(
			List.of(new ImageGenerationRequestDTO.Contents(
				List.of(new ImageGenerationRequestDTO.Part(prompt)))),
			new ImageGenerationRequestDTO.GenerationConfig(List.of("IMAGE")));

		return webClient.post()
			.uri(uriBuilder -> uriBuilder
				.path("/v1beta/models/gemini-2.5-flash-image-preview:generateContent")
				.queryParam("key", apiKey)
				.build())
			.bodyValue(request)
			.retrieve()
			// 우선 String으로 받아 raw 로그 찍기
			.bodyToMono(String.class)
			.doOnNext(raw -> System.out.println("Raw Gemini Response: " + raw))
			// 다시 DTO로 변환
			.flatMap(raw -> webClient.post()
				.uri(uriBuilder -> uriBuilder
					.path("/v1beta/models/gemini-2.5-flash-image-preview:generateContent")
					.queryParam("key", apiKey)
					.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ImageGenerationResponseDTO.class))
			.map(response -> {
				var candidate = response.getCandidates().get(0);
				var part = candidate.getContent().getParts().get(0);

				if (part.getInlineData() != null) {
					return part.getInlineData().getData(); // Base64 이미지
				} else if (part.getText() != null) {
					return "[TEXT RESPONSE] " + part.getText(); // 텍스트 fallback
				} else {
					throw new RuntimeException("Gemini response has no usable content.");
				}
			})
			.doOnError(e -> System.err.println("Error calling Gemini API: " + e.getMessage()));
	}
}

