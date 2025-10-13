package com.miracle.coordifit.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Base64ImageDto {
	// 예) "data:image/png;base64,iVBORw0K..."
	private String dataUrl;
	// S3에 저장할 원본 파일명(확장자 포함) – 선택
	private String fileName;
}
