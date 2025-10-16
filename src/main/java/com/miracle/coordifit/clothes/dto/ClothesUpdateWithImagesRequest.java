package com.miracle.coordifit.clothes.dto;

import java.time.LocalDate;
import java.util.List;

import com.miracle.coordifit.common.dto.Base64ImageDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothesUpdateWithImagesRequest {
	private String clothesId;
	private String name;
	private String categoryCode;
	private String brand;
	private String clothesSize;
	private Integer price;
	private LocalDate purchaseDate;
	private String purchaseUrl;
	private String description;

	// 새로 추가/교체할 이미지(Base64)
	private List<Base64ImageDto> images;

	// true면 기존 링크 전부 삭제 후 images를 새로 연결 (기본 false)
	private Boolean replaceAllImages;
	private LocalDate lastWornDate;
}
