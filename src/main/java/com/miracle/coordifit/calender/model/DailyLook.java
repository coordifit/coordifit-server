package com.miracle.coordifit.calender.model;

import lombok.Data;

@Data
public class DailyLook {
	private String dailylookId;
	private String userId;
	private String wearDate;
	private String description;
	private Integer originImageId;
	private Integer thumbImageId;
	private String canvasJson;
	private String createdAt;
	private String updatedAt;
}
