package com.miracle.coordifit.coordi.model;

import java.sql.Date;

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
	private String coordiName;
	private String description;
	private String canvasJson;
	private Date lastWornDate;
	private Integer fileId;
	private Integer originImageId;
	private Integer thumbImageId;
	private String createdBy;
	private String isFavorite;
	private String isActive;
	private Integer wearCount;
	private String updatedBy;
	private Date createdAt;
	private Date updatedAt;

	public static Coordi empty() {
		return Coordi.builder().build();
	}
}
