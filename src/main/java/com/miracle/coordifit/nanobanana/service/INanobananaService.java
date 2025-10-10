package com.miracle.coordifit.nanobanana.service;

import com.miracle.coordifit.nanobanana.dto.ImageGenerationRequestDTO;

import reactor.core.publisher.Mono;

public interface INanobananaService {
	Mono<String> generateImage(ImageGenerationRequestDTO requestDTO);
}
