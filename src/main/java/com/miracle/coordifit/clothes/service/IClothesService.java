// src/main/java/com/miracle/coordifit/clothes/service/IClothesService.java
package com.miracle.coordifit.clothes.service;

import java.util.List;

import com.miracle.coordifit.clothes.dto.*;
import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.common.model.FileInfo;

public interface IClothesService {

	// ===== Base64 등록/수정 =====
	String createOneBase64(ClothesCreateWithImagesRequest req, String actor);

	List<String> bulkCreateBase64(ClothesBulkCreateWithImagesRequest req, String actor);

	void updateBase64(String clothesId, ClothesUpdateWithImagesRequest req, String actor);

	// ===== 조회/삭제 =====
	ClothesDetailDto findDetail(String clothesId);

	List<ClothesListItemDto> findAllByUser(String userId);

	List<ClothesDetailDto> getClothes(String categoryId, String subCategoryId);

	void remove(String clothesId); // 소프트삭제(+이미지 링크 정리)

	void removeImage(String clothesId, Long fileId);

	// (기존 유지: 엔티티/파일 조회)
	Clothes findOne(String clothesId);

	List<Clothes> findMine(String userId);

	List<FileInfo> findImages(String clothesId);

	void bulkDelete(List<String> clothesIds);

	List<String> bulkCreateBase64Parallel(ClothesBulkCreateWithImagesRequest req, String actor);
}
