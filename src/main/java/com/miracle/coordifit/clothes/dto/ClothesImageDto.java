// src/main/java/com/miracle/coordifit/clothes/dto/ClothesImageDto.java
package com.miracle.coordifit.clothes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"fileId", "url", "isPrimary", "order"})
public class ClothesImageDto {
	private Integer fileId;
	private String url;
	private Boolean isPrimary;
	private Integer order;
}
