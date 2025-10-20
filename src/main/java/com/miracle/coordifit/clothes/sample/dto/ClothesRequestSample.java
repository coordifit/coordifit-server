package com.miracle.coordifit.clothes.sample.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClothesRequestSample {
	private String name;
	private String brand;
	private String categoryCode;
	private String clothesSize;
	private Integer price;
	private String purchaseDate;
	private String purchaseUrl;
	private String description;
	private List<Long> deletedFileIds;
	private List<MultipartFile> files;
}
