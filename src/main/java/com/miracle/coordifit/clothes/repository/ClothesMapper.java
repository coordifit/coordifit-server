package com.miracle.coordifit.clothes.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.clothes.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.dto.CodeDto;
import com.miracle.coordifit.clothes.model.Clothes;

@Mapper
public interface ClothesMapper {
	// Clothes
	int insertClothes(Clothes clothes);

	ClothesDetailResponseDto selectClothesDetail(@Param("clothesId")
	String clothesId);

	List<ClothesDetailResponseDto> selectClothesList(@Param("userId")
	String userId,
		@Param("categoryCode")
		String categoryCode);

	// Link(CLOTHES_IMAGES)
	int insertClothesImages(@Param("clothesId")
	String clothesId,
		@Param("fileIds")
		List<Long> fileIds);

	List<String> selectClothesImageUrls(@Param("clothesId")
	String clothesId);

	// Categories (COMMON_CODES)
	List<CodeDto> selectTopCategories();

	List<CodeDto> selectChildren(@Param("parentCodeId")
	String parentCodeId);

	CodeDto selectOne(@Param("codeId")
	String codeId);

	// FILE_INFO helpers (image 패키지 손대지 않기 위해 사용)
	Long selectMaxFileId();

	List<Long> selectFileIdsAfter(@Param("lastFileId")
	Long lastFileId);
}
