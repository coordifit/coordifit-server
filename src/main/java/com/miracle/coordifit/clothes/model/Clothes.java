package com.miracle.coordifit.clothes.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clothes {
	private String clothesId;
	private String userId;
	private String name;
	private String brand;
	private String categoryCode;
	private String clothesSize;
	private BigDecimal price;
	private LocalDate purchaseDate;
	private String purchaseUrl;
	private String description;
	private String isActive;
	private String createdBy;
}
