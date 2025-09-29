package com.miracle.coordifit.nanobanana.service;

import reactor.core.publisher.Mono;

/**
 * Google Gemini (nano-banana) API를 사용하여 이미지를 생성하는 서비스에 대한
 * 인터페이스를 정의합니다.
 */
public interface NanobananaService {

	/**
	 * 텍스트 프롬프트를 사용하여 이미지를 생성하고 base64 문자열로 반환합니다.
	 *
	 * @param prompt 이미지 생성에 사용될 텍스트 프롬프트
	 * @return Mono<String> Base64 인코딩된 이미지 데이터
	 */
	Mono<String> generateImage(String prompt);
}
