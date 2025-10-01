package com.miracle.coordifit.clothes.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothesBulkCreateRequest {

	@NotEmpty
	private List<ClothesCreateRequest> items;
}
