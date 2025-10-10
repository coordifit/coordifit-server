// src/main/java/com/miracle/coordifit/clothes/service/ClothesService.java
package com.miracle.coordifit.clothes.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.clothes.dto.ClothesBulkCreateRequest;
import com.miracle.coordifit.clothes.dto.ClothesCreateRequest;
import com.miracle.coordifit.clothes.dto.ClothesResponse;
import com.miracle.coordifit.clothes.dto.ClothesUpdateRequest;
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

		Integer next = clothesRepository.getNextClothesDailySeq();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		String clothesId = String.format("C%s%03d", date, next);

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

	// [ADD] 공통: 오늘 기준 "CyyMMdd###" ID 생성
	private String nextClothesId() {
		String ymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		int seq = clothesRepository.getNextClothesDailySeq();
		return "C" + ymd + String.format("%03d", seq);
	}

	// [ADD] OCR/검수 확정(파일 이미 업로드되어 fileId만 넘어오는) 다중등록
	@Transactional
	@Override
	public List<String> bulkCreate(ClothesBulkCreateRequest req, String actor) {
		List<Clothes> clothesList = new ArrayList<>();
		List<ClothesImageLink> links = new ArrayList<>();
		List<String> createdIds = new ArrayList<>();

		if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
			return createdIds;
		}

		final String ymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		// ✅ 하루 시퀀스 “한 번만” 읽기
		int base = clothesRepository.getNextClothesDailySeq();

		int i = 0;
		for (ClothesCreateRequest item : req.getItems()) {
			String clothesId = String.format("C%s%03d", ymd, base + i++); // ✅ 각 항목마다 +i
			createdIds.add(clothesId);

			Clothes c = new Clothes();
			c.setClothesId(clothesId);
			c.setUserId(item.getUserId());
			c.setName(item.getName());
			c.setBrand(item.getBrand());
			c.setCategoryCode(item.getCategoryCode());
			c.setClothesSize(item.getClothesSize());
			c.setPrice(item.getPrice());
			c.setPurchaseDate(item.getPurchaseDate());
			c.setPurchaseUrl(item.getPurchaseUrl());
			c.setDescription(item.getDescription());
			if (c.getWearCount() == null)
				c.setWearCount(0);
			c.setIsActive("Y");
			c.setCreatedBy(actor);
			c.setUpdatedBy(actor);
			clothesList.add(c);

			if (item.getFileIds() != null) {
				for (Long fid : item.getFileIds()) {
					if (fid == null)
						continue;
					links.add(ClothesImageLink.builder()
						.clothesId(clothesId)
						.fileId(fid)
						.createdBy(actor)
						.build());
				}
			}
		}

		if (!clothesList.isEmpty())
			clothesRepository.insertBulkClothes(clothesList);
		if (!links.isEmpty())
			clothesRepository.insertBulkImageLinks(links);

		return createdIds;
	}

	// [ADD] 단건 DTO 등록(내부적으로 bulkCreate 재사용)
	@Transactional
	@Override
	public String createOne(ClothesCreateRequest req, String actor) {
		ClothesBulkCreateRequest wrap = new ClothesBulkCreateRequest();
		wrap.setItems(List.of(req));
		List<String> created = bulkCreate(wrap, actor);
		return created.isEmpty() ? null : created.get(0);
	}

	// [ADD] DTO 기반 수정(파일은 fileId로 교체/추가)
	@Transactional
	@Override
	public void update(ClothesUpdateRequest req, boolean replaceFiles, String actor) {
		// 1) 부분 업데이트
		Clothes c = new Clothes();
		c.setClothesId(req.getClothesId());
		c.setName(req.getName());
		c.setBrand(req.getBrand());
		c.setCategoryCode(req.getCategoryCode());
		c.setClothesSize(req.getClothesSize());
		c.setPrice(req.getPrice());
		c.setPurchaseDate(req.getPurchaseDate());
		c.setPurchaseUrl(req.getPurchaseUrl());
		c.setDescription(req.getDescription());
		c.setUpdatedBy(actor);
		clothesRepository.updateClothes(c);

		// 2) 파일 링크 처리
		if (req.getFileIds() != null) {
			if (replaceFiles) {
				clothesRepository.deleteAllImageLinks(req.getClothesId());
			}
			if (!req.getFileIds().isEmpty()) {
				List<ClothesImageLink> links = new ArrayList<>();
				for (Long fid : req.getFileIds()) {
					if (fid == null)
						continue;
					links.add(
						ClothesImageLink.builder()
							.clothesId(req.getClothesId())
							.fileId(fid)
							.createdBy(actor)
							.build());
				}
				clothesRepository.insertBulkImageLinks(links);
			}
		}
	}

	// [ADD] 다중 삭제(옷 + 연결 이미지 링크)
	@Transactional
	@Override
	public void bulkDelete(List<String> clothesIds) {
		if (clothesIds == null || clothesIds.isEmpty())
			return;
		clothesRepository.deleteImagesByClothesIds(clothesIds);
		clothesRepository.deleteClothesByIds(clothesIds);
	}

	public List<ClothesResponse> getClothes(String categoryId, String subCategoryId) {
		if (subCategoryId != null && !subCategoryId.isEmpty()) {
			// 하위 카테고리 조회
			return clothesRepository.findBySubCategory(subCategoryId);
		} else if (categoryId != null && !categoryId.isEmpty()) {
			// 상위 카테고리 → 하위 전체 포함
			return clothesRepository.findByCategory(categoryId);
		} else {
			// 전체 조회
			return clothesRepository.findAllClothes();
		}
	}
}
