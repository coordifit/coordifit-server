package com.miracle.coordifit.clothes.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"clothesId", "name", "categoryCode", "purchaseDate", "thumbnailUrl",
	"description", "brand", "size", "price", "purchaseUrl", "images"
})
public class ClothesDetailDto {
	private String clothesId;
	private String name;
	private String categoryCode;
	private String categoryName;
	private LocalDate purchaseDate;
	private String thumbnailUrl;
	private String description;
	private String brand;
	private String clothesSize;
	private Integer price;
	private String purchaseUrl;
	private LocalDate lastWornDate;
	private List<ClothesImageDto> images;

}
