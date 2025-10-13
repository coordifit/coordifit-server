package com.miracle.coordifit.calender.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyLookResponse {
	private String dailylookId;
	private String userId;
	private String wearDate;
	private String description;
	private String canvasJson;
	private String originImageUrl;
	private String thumbImageUrl;

	public static DailyLookResponse empty() {
		return DailyLookResponse.builder().build();
	}
}
