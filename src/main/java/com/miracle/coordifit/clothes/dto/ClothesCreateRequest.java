package com.miracle.coordifit.clothes.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesCreateRequest {
	@NotBlank
	private String name;
	@NotBlank
	private String categoryCode;
	@NotBlank
	private String userId;
	private String brand;
	private String clothesSize;
	private Integer price;
	private LocalDate purchaseDate;
	private String purchaseUrl;
	private String description;

	private List<Long> fileIds;

}
