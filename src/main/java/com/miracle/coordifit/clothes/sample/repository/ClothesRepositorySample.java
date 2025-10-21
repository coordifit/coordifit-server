package com.miracle.coordifit.clothes.sample.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.clothes.sample.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.sample.dto.ClothesResponseSample;
import com.miracle.coordifit.clothes.sample.model.ClothesImageSample;
import com.miracle.coordifit.clothes.sample.model.ClothesSample;

@Mapper
public interface ClothesRepositorySample {

	int getNextClothesSequence();

	int insertClothes(ClothesSample clothes);

	int updateClothes(ClothesSample clothes);

	int insertClothesImage(ClothesImageSample clothesImage);

	List<ClothesResponseSample> selectUserClothes(@Param("userId") String userId);

	ClothesDetailResponseDto selectClothesById(@Param("clothesId") String clothesId, @Param("userId") String userId);

	List<ClothesDetailResponseDto.ClothesImage> selectClothesImage(@Param("clothesId") String clothesId);

	int deleteClothesImage(@Param("clothesId") String clothesId, @Param("fileId") Long fileId);
}
