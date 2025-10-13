package com.miracle.coordifit.clothes.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
public class ClothesBulkCreateWithImagesRequest {
	@NotEmpty
	private List<ClothesCreateWithImagesRequest> items;
}
