package com.miracle.coordifit.clothes.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"clothesId", "name", "categoryCode", "purchaseDate", "thumbnailUrl"})
public class ClothesListItemDto {
	private String clothesId;
	private String name;
	private String categoryCode;
	private LocalDate purchaseDate;
	private String thumbnailUrl;
	private LocalDate lastWornDate;
}
