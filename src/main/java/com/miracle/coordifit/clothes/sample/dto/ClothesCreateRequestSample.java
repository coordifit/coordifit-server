package com.miracle.coordifit.clothes.sample.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ClothesCreateRequestSample {
	private String name;
	private String brand;
	private String categoryCode;
	private String clothesSize;
	private Integer price;
	private String purchaseDate;
	private String purchaseUrl;
	private String description;
	private List<MultipartFile> files;
}
