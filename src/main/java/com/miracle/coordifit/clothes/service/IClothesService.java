package com.miracle.coordifit.clothes.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.common.model.FileInfo;

public interface IClothesService {
	String register(Clothes clothes, List<MultipartFile> images, String userId);

	void modify(Clothes clothes, List<MultipartFile> addImages, boolean replaceAllImages, String updaterId);

	void remove(String clothesId);

	Clothes findOne(String clothesId);

	List<Clothes> findMine(String userId);

	List<FileInfo> findImages(String clothesId);

	void removeImage(String clothesId, Long fileId);
}
