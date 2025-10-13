package com.miracle.coordifit.clothes.dto;

import java.time.LocalDate;
import java.util.List;

import com.miracle.coordifit.common.dto.Base64ImageDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothesCreateWithImagesRequest {
	@NotBlank
	private String userId;
	@NotBlank
	private String name;
	@NotBlank
	private String categoryCode;

	private String brand;
	private String clothesSize;
	private Integer price;
	private LocalDate purchaseDate;
	private String purchaseUrl;
	private String description;

	// 업로드할 이미지(Base64) 1~N
	private List<Base64ImageDto> images;
}
