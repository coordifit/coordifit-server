package com.miracle.coordifit.coordi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coordi {
	private String coordiId;
	private String userId;
	private String title;
	private String description;
	private String canvasJson;
	private Integer originImageId;
	private Integer thumbImageId;

	public static Coordi empty() {
		return Coordi.builder().build();
	}
}
