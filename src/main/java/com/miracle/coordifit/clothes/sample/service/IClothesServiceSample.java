package com.miracle.coordifit.clothes.sample.service;

import java.util.List;

import com.miracle.coordifit.clothes.sample.dto.ClothesCreateRequestSample;
import com.miracle.coordifit.clothes.sample.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.sample.dto.ClothesResponseSample;

public interface IClothesServiceSample {

	String createClothes(ClothesCreateRequestSample request, String userId);

	List<ClothesResponseSample> getUserClothes(String userId);

	ClothesDetailResponseDto getClothesDetail(String clothesId, String userId);
}
