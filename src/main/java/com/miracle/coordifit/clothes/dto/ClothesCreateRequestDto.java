package com.miracle.coordifit.clothes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesCreateRequestDto {
	@NotBlank
	private String categoryCode; // B3xxxx (세부)
	@NotBlank
	@Size(max = 100)
	private String name;
	@Size(max = 100)
	private String brand;
	@Size(max = 20)
	private String clothesSize;
	private BigDecimal price; // NUMBER(10)
	private LocalDate purchaseDate; // YYYY-MM-DD
	@Size(max = 1000)
	private String purchaseUrl;
	@Size(max = 1000)
	private String description;
}
