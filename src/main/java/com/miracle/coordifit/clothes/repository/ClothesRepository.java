package com.miracle.coordifit.clothes.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.clothes.dto.ClothesDetailDto;
import com.miracle.coordifit.clothes.dto.ClothesListItemDto;
import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.model.ClothesImageLink;
import com.miracle.coordifit.common.model.FileInfo;

@Mapper
public interface ClothesRepository {

	// ===== 공통/유틸 =====
	int getNextClothesDailySeq();

	// ===== 쓰기 =====
	int insertClothes(Clothes clothes);

	int updateClothes(Clothes clothes);

	// 소프트삭제
	int deleteClothes(@Param("clothesId") String clothesId);

	int deleteClothesByIds(@Param("ids") List<String> clothesIds);

	// ===== 이미지 링크 =====
	int insertImageLink(ClothesImageLink link);

	int insertBulkImageLinks(@Param("links") List<ClothesImageLink> links);

	int deleteAllImageLinks(@Param("clothesId") String clothesId);

	int deleteImageLink(@Param("clothesId") String clothesId, @Param("fileId") Long fileId);

	int deleteImagesByClothesIds(@Param("ids") List<String> clothesIds);

	List<ClothesImageLink> findImageLinks(@Param("clothesId") String clothesId);

	int countImageLinks(@Param("clothesId") String clothesId);

	List<FileInfo> findImageFiles(@Param("clothesId") String clothesId);

	// ===== 조회(엔티티) =====
	Clothes findById(@Param("clothesId") String clothesId);

	List<Clothes> findAllByUser(@Param("userId") String userId);

	// ===== 벌크 =====
	//int insertBulkClothes(@Param("list") List<Clothes> clothesList);//

	// ===== DTO 조회 =====
	ClothesDetailDto findDetailById(@Param("clothesId") String clothesId);

	List<ClothesDetailDto> findAllClothes();

	List<ClothesDetailDto> findByCategory(@Param("categoryId") String categoryId);

	List<ClothesDetailDto> findBySubCategory(@Param("subCategoryId") String subCategoryId);

	List<ClothesListItemDto> findAllListByUser(@Param("userId") String userId);

	int existsActiveCategoryCount(@Param("categoryCode") String categoryCode);

	String selectNextClothesId();

}
