package com.miracle.coordifit.clothes.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Clothes {
	private String clothesId;
	private String userId;
	private String name;
	private String brand;
	private String categoryCode;
	private String clothesSize;
	private Integer price;
	private LocalDate purchaseDate;
	private String purchaseUrl;
	private String description;
	private Integer wearCount;
	private LocalDate lastWornDate;
	private String isActive;
	private String createdBy;
	private String updatedBy;
}
