package com.miracle.coordifit.nanobanana.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.miracle.coordifit.nanobanana.dto.ImageGenerationRequestDTO;
import com.miracle.coordifit.nanobanana.dto.ImageGenerationResponseDTO;

import reactor.core.publisher.Mono;

/**
 * NanobananaService 인터페이스의 구현체입니다.
 * Google Gemini (nano-banana) API를 호출하여 이미지를 생성하는 실제 로직을 담고 있습니다.
 */
@Service
public class NanobananaServiceImpl implements NanobananaService {

	private final WebClient webClient;
	private final String apiKey;

	public NanobananaServiceImpl(@Qualifier("geminiWebClient") WebClient webClient,
		@Value("${app.google.gemini.api-key}")
		String apiKey) {
		this.webClient = webClient;
		this.apiKey = apiKey;
	}

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
