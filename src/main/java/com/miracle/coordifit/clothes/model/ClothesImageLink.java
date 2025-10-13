package com.miracle.coordifit.clothes.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothesImageLink {
	private String clothesId;
	private long fileId;
	private String createdBy;
}
