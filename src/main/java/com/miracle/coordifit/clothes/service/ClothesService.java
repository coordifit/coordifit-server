// src/main/java/com/miracle/coordifit/clothes/service/ClothesService.java
package com.miracle.coordifit.clothes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.model.ClothesImageLink;
import com.miracle.coordifit.clothes.repository.ClothesRepository;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClothesService implements IClothesService {

	private final ClothesRepository clothesRepository;
	private final IFileService fileService;

	@Override
	@Transactional
	public String register(Clothes clothes, List<MultipartFile> images, String userId) {

		int cnt = (images == null) ? 0 : images.size();
		if (cnt < 1 || cnt > 5) {
			throw new IllegalArgumentException("이미지는 1~5장 업로드해야 합니다.");
		}

		clothes.setUserId(userId);
		clothes.setCreatedBy(userId);
		clothes.setUpdatedBy(userId);
		if (clothes.getWearCount() == null)
			clothes.setWearCount(0);

		Integer next = clothesRepository.getNextClothesSeq();
		String clothesId = String.format("C%05d", next);

		clothes.setClothesId(clothesId);

		clothesRepository.insertClothes(clothes);

		for (MultipartFile file : images) {
			if (file == null || file.isEmpty())
				continue;

			FileInfo saved = fileService.uploadFile(file);
			if (saved != null && saved.getFileId() != null) {
				clothesRepository.insertImageLink(
					ClothesImageLink.builder()
						.clothesId(clothesId)
						.fileId(saved.getFileId().longValue())
						.createdBy(userId)
						.build());
			}
		}
		return clothesId;
	}

	@Override
	@Transactional
	public void modify(Clothes clothes, List<MultipartFile> addImages, boolean replaceAllImages, String userId) {
		clothes.setUpdatedBy(userId);
		if (clothes.getWearCount() == null)
			clothes.setWearCount(0);

		clothesRepository.updateClothes(clothes);

		if (replaceAllImages) {
			clothesRepository.deleteAllImageLinks(clothes.getClothesId());
		}

		if (addImages != null) {
			for (MultipartFile file : addImages) {
				if (file == null || file.isEmpty())
					continue;

				FileInfo saved = fileService.uploadFile(file);
				if (saved != null && saved.getFileId() != null) {
					clothesRepository.insertImageLink(
						ClothesImageLink.builder()
							.clothesId(clothes.getClothesId())
							.fileId(saved.getFileId().longValue())
							.createdBy(userId)
							.build());
				}
			}
		}
	}

	@Override
	@Transactional
	public void remove(String clothesId) {
		clothesRepository.deleteClothes(clothesId);
	}

	@Override
	@Transactional
	public void removeImage(String clothesId, Long fileId) {
		clothesRepository.deleteImageLink(clothesId, fileId);
	}

	@Override
	public Clothes findOne(String clothesId) {
		return clothesRepository.findById(clothesId);
	}

	@Override
	public List<FileInfo> findImages(String clothesId) {
		return clothesRepository.findImageFiles(clothesId);
	}

	@Override
	public List<Clothes> findMine(String userId) {
		return clothesRepository.findAllByUser(userId);
	}
}
