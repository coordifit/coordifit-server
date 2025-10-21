package com.miracle.coordifit.clothes.sample.service;

import java.util.List;

import com.miracle.coordifit.clothes.sample.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.sample.dto.ClothesRequestSample;
import com.miracle.coordifit.clothes.sample.dto.ClothesResponseSample;
import com.miracle.coordifit.clothes.sample.model.ClothesSample;

public interface IClothesServiceSample {

	ClothesSample createClothes(ClothesRequestSample request, String userId);

	ClothesSample updateClothes(String clothesId, ClothesRequestSample request, String userId);

	List<ClothesResponseSample> getUserClothes(String userId);

	ClothesDetailResponseDto getClothesDetail(String clothesId, String userId);

	ClothesSample deleteClothes(String clothesId, String userId);

	List<ClothesSample> bulkDeleteClothes(List<String> clothesIds, String userId);
}
