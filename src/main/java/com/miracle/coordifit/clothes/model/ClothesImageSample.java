package com.miracle.coordifit.clothes.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothesImageSample {
	private String clothesId;
	private Long fileId;
	private LocalDateTime createdAt;
	private String createdBy;
}
