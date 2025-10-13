// src/main/java/com/miracle/coordifit/clothes/service/ClothesService.java
package com.miracle.coordifit.clothes.service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.miracle.coordifit.clothes.dto.*;
import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.model.ClothesImageLink;
import com.miracle.coordifit.clothes.repository.ClothesRepository;
import com.miracle.coordifit.common.dto.Base64ImageDto;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.ICommonCodeService;
import com.miracle.coordifit.common.service.IFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClothesService implements IClothesService {

	private final ClothesRepository clothesRepository;
	private final IFileService fileService;
	private final ICommonCodeService commonCodeService;
	private final TransactionTemplate transactionTemplate; // 병렬 항목별 트랜잭션용

	// ================== 등록(단건) Base64 ==================
	@Override
	@Transactional
	public String createOneBase64(ClothesCreateWithImagesRequest req, String actor) {
		if (req.getImages() == null || req.getImages().isEmpty()) {
			throw new IllegalArgumentException("이미지는 최소 1장 필요합니다.");
		}
		validateCategoryOrThrow(req.getCategoryCode());

		final String id = clothesRepository.selectNextClothesId();

		Clothes c = new Clothes();
		c.setClothesId(id);
		c.setUserId(req.getUserId());
		c.setName(req.getName());
		c.setBrand(req.getBrand());
		c.setCategoryCode(req.getCategoryCode());
		c.setClothesSize(req.getClothesSize());
		c.setPrice(req.getPrice());
		c.setPurchaseDate(req.getPurchaseDate());
		c.setPurchaseUrl(req.getPurchaseUrl());
		c.setDescription(req.getDescription());
		c.setWearCount(0);
		c.setIsActive("Y");
		c.setCreatedBy(actor);
		c.setUpdatedBy(actor);

		clothesRepository.insertClothes(c);

		List<FileInfo> saved = fileService.uploadBase64Batch(req.getImages()); // FileService 안에서 공백/개행 제거 권장
		linkImagesBatch(id, saved, actor);
		return id;
	}

	// ================== 등록(벌크) Base64 - 순차 ==================
	@Override
	public List<String> bulkCreateBase64(ClothesBulkCreateWithImagesRequest req, String actor) {
		if (req == null || req.getItems() == null || req.getItems().isEmpty())
			return List.of();

		List<String> ids = new ArrayList<>();
		for (ClothesCreateWithImagesRequest item : req.getItems()) {
			if (item.getImages() == null || item.getImages().isEmpty()) {
				throw new IllegalArgumentException("각 항목은 최소 1장 이미지가 필요합니다.");
			}
			validateCategoryOrThrow(item.getCategoryCode());

			final String id = clothesRepository.selectNextClothesId(); // ✅
			ids.add(id);

			transactionTemplate.execute(status -> {
				try {
					Clothes c = new Clothes();
					c.setClothesId(id);
					c.setUserId(item.getUserId());
					c.setName(item.getName());
					c.setBrand(item.getBrand());
					c.setCategoryCode(item.getCategoryCode());
					c.setClothesSize(item.getClothesSize());
					c.setPrice(item.getPrice());
					c.setPurchaseDate(item.getPurchaseDate());
					c.setPurchaseUrl(item.getPurchaseUrl());
					c.setDescription(item.getDescription());
					c.setWearCount(0);
					c.setIsActive("Y");
					c.setCreatedBy(actor);
					c.setUpdatedBy(actor);

					clothesRepository.insertClothes(c);
					List<FileInfo> saved = fileService.uploadBase64Batch(item.getImages());
					linkImagesBatch(id, saved, actor);
				} catch (Exception e) {
					status.setRollbackOnly();
					throw e;
				}
				return null;
			});
		}
		return ids;
	}

	// ================== 등록(벌크) Base64 - 병렬 ==================
	// ⚠️ 여기서는 메서드 전체 @Transactional 금지 (각 작업을 개별 트랜잭션으로)
	@Override
	public List<String> bulkCreateBase64Parallel(ClothesBulkCreateWithImagesRequest req, String actor) {
		if (req == null || req.getItems() == null || req.getItems().isEmpty())
			return List.of();

		int threads = Math.min(4, Math.max(1, req.getItems().size()));
		ExecutorService es = Executors.newFixedThreadPool(threads);
		List<Future<String>> futures = new ArrayList<>();

		for (ClothesCreateWithImagesRequest item : req.getItems()) {
			futures.add(es.submit(() -> transactionTemplate.execute(status -> {
				try {
					if (item.getImages() == null || item.getImages().isEmpty()) {
						throw new IllegalArgumentException("각 항목은 최소 1장 이미지 필요");
					}
					validateCategoryOrThrow(item.getCategoryCode());

					final String id = clothesRepository.selectNextClothesId(); // ✅ 매 건마다

					Clothes c = new Clothes();
					c.setClothesId(id);
					c.setUserId(item.getUserId());
					c.setName(item.getName());
					c.setBrand(item.getBrand());
					c.setCategoryCode(item.getCategoryCode());
					c.setClothesSize(item.getClothesSize());
					c.setPrice(item.getPrice());
					c.setPurchaseDate(item.getPurchaseDate());
					c.setPurchaseUrl(item.getPurchaseUrl());
					c.setDescription(item.getDescription());
					c.setWearCount(0);
					c.setIsActive("Y");
					c.setCreatedBy(actor);
					c.setUpdatedBy(actor);

					clothesRepository.insertClothes(c);
					List<FileInfo> saved = fileService.uploadBase64Batch(item.getImages());
					linkImagesBatch(id, saved, actor);
					return id;
				} catch (Exception e) {
					status.setRollbackOnly();
					throw new RuntimeException("벌크 항목 실패: " + item.getName() + " - " + e.getMessage(), e);
				}
			})));
		}
		es.shutdown();

		// ✅ 하나라도 실패하면 전체 실패로 반환 (200 방지)
		List<String> ids = new ArrayList<>();
		for (Future<String> f : futures) {
			try {
				ids.add(f.get()); // 여기서 ExecutionException이면 catch로
			} catch (Exception e) {
				es.shutdownNow();
				throw new RuntimeException("벌크 등록 중 일부 실패", e.getCause() != null ? e.getCause() : e);
			}
		}
		return ids;
	}

	// ================== 수정 Base64 ==================
	@Override
	@Transactional
	public void updateBase64(String clothesId, ClothesUpdateWithImagesRequest req, String actor) {
		Clothes c = new Clothes();
		c.setClothesId(clothesId);
		c.setName(req.getName());
		c.setBrand(req.getBrand());
		c.setCategoryCode(req.getCategoryCode());
		c.setClothesSize(req.getClothesSize());
		c.setPrice(req.getPrice());
		c.setPurchaseDate(req.getPurchaseDate());
		c.setPurchaseUrl(req.getPurchaseUrl());
		c.setDescription(req.getDescription());
		c.setUpdatedBy(actor);

		if (req.getCategoryCode() != null)
			validateCategoryOrThrow(req.getCategoryCode());
		clothesRepository.updateClothes(c);

		List<Base64ImageDto> imgs = req.getImages();
		boolean replace = Boolean.TRUE.equals(req.getReplaceAllImages());

		if (imgs != null) {
			if (replace) {
				clothesRepository.deleteAllImageLinks(clothesId);
			}
			if (!imgs.isEmpty()) {
				List<FileInfo> saved = fileService.uploadBase64Batch(imgs);
				linkImagesBatch(clothesId, saved, actor);
			}
		}
	}

	// ================== 삭제/조회 ==================
	@Override
	@Transactional
	public void remove(String clothesId) {
		clothesRepository.deleteAllImageLinks(clothesId);
		clothesRepository.deleteClothes(clothesId);
	}

	@Override
	@Transactional
	public void bulkDelete(List<String> clothesIds) {
		if (clothesIds == null || clothesIds.isEmpty())
			return;
		clothesRepository.deleteImagesByClothesIds(clothesIds);
		clothesRepository.deleteClothesByIds(clothesIds);
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

	@Override
	public ClothesDetailDto findDetail(String clothesId) {
		ClothesDetailDto dto = clothesRepository.findDetailById(clothesId);
		if (dto == null)
			throw new RuntimeException("해당 clothesId 없음: " + clothesId);

		List<FileInfo> files = clothesRepository.findImageFiles(clothesId);
		if (files == null)
			files = List.of();

		String thumbnail = files.stream()
			.min(Comparator.comparing(FileInfo::getFileId))
			.map(FileInfo::getS3Url).orElse(null);
		dto.setThumbnailUrl(thumbnail);

		dto.setImages(files.stream().map(f -> ClothesImageDto.builder()
			.fileId(f.getFileId()).url(f.getS3Url()).build()).collect(Collectors.toList()));

		try {
			var codes = commonCodeService.getCommonCodes();
			var cc = (codes != null) ? codes.get(dto.getCategoryCode()) : null;
			if (cc != null)
				dto.setCategoryName(cc.getCodeName());
		} catch (Exception ignore) {}
		return dto;
	}

	@Override
	public List<ClothesDetailDto> getClothes(String categoryId, String subCategoryId) {
		List<ClothesDetailDto> list;
		if (subCategoryId != null && !subCategoryId.isEmpty()) {
			list = clothesRepository.findBySubCategory(subCategoryId);
		} else if (categoryId != null && !categoryId.isEmpty()) {
			list = clothesRepository.findByCategory(categoryId);
		} else {
			list = clothesRepository.findAllClothes();
		}

		for (ClothesDetailDto dto : list) {
			List<FileInfo> files = clothesRepository.findImageFiles(dto.getClothesId());
			String thumbnail = files.stream()
				.min(Comparator.comparing(FileInfo::getFileId))
				.map(FileInfo::getS3Url).orElse(null);
			dto.setThumbnailUrl(thumbnail);
			dto.setImages(files.stream().map(f -> ClothesImageDto.builder()
				.fileId(f.getFileId()).url(f.getS3Url()).build()).collect(Collectors.toList()));
		}
		return list;
	}

	private void linkImagesBatch(String clothesId, List<FileInfo> files, String actor) {
		if (files == null || files.isEmpty())
			return;
		List<ClothesImageLink> links = new ArrayList<>(files.size());
		for (FileInfo f : files) {
			links.add(ClothesImageLink.builder()
				.clothesId(clothesId)
				.fileId(f.getFileId())
				.createdBy(actor)
				.build());
		}
		clothesRepository.insertBulkImageLinks(links);
	}

	private void validateCategoryOrThrow(String categoryCode) {
		if (categoryCode == null || categoryCode.isBlank()) {
			throw new IllegalArgumentException("categoryCode는 필수입니다.");
		}
		int cnt = clothesRepository.existsActiveCategoryCount(categoryCode);
		if (cnt <= 0)
			throw new IllegalArgumentException("유효하지 않은 categoryCode: " + categoryCode);
	}

	// 목록 카드용(필요시 구현)
	@Override
	public List<ClothesListItemDto> findAllByUser(String userId) {
		return clothesRepository.findAllListByUser(userId);
	}
}
