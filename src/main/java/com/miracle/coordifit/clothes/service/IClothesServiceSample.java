package com.miracle.coordifit.clothes.service;

import java.util.List;

import com.miracle.coordifit.clothes.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.dto.ClothesRequestSample;
import com.miracle.coordifit.clothes.dto.ClothesResponseSample;
import com.miracle.coordifit.clothes.model.ClothesSample;

public interface IClothesServiceSample {

	ClothesSample createClothes(ClothesRequestSample request, String userId);

	ClothesSample updateClothes(String clothesId, ClothesRequestSample request, String userId);

	List<ClothesResponseSample> getUserClothes(String userId);

	ClothesDetailResponseDto getClothesDetail(String clothesId, String userId);

	ClothesSample deleteClothes(String clothesId, String userId);

	List<ClothesSample> bulkDeleteClothes(List<String> clothesIds, String userId);
}
