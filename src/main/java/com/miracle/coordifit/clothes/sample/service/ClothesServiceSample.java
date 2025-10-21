package com.miracle.coordifit.clothes.sample.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.clothes.sample.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.sample.dto.ClothesRequestSample;
import com.miracle.coordifit.clothes.sample.dto.ClothesResponseSample;
import com.miracle.coordifit.clothes.sample.model.ClothesImageSample;
import com.miracle.coordifit.clothes.sample.model.ClothesSample;
import com.miracle.coordifit.clothes.sample.repository.ClothesRepositorySample;
import com.miracle.coordifit.common.aspect.SaveHistory;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClothesServiceSample implements IClothesServiceSample {

	private final ClothesRepositorySample clothesRepository;
	private final IFileService fileService;

	@Override
	@Transactional
	@SaveHistory(entityType = "CLOTHES", actionType = "INSERT")
	public ClothesSample createClothes(ClothesRequestSample request, String userId) {
		if (request.getFiles() == null || request.getFiles().isEmpty()) {
			throw new IllegalArgumentException("이미지는 최소 1장 필요합니다.");
		}

		log.info("옷 등록 시작: userId={}, name={}", userId, request.getName());

		String clothesId = generateClothesId();

		ClothesSample clothes = ClothesSample.builder()
			.clothesId(clothesId)
			.userId(userId)
			.name(request.getName())
			.brand(request.getBrand())
			.categoryCode(request.getCategoryCode())
			.clothesSize(request.getClothesSize())
			.price(request.getPrice())
			.purchaseDate(request.getPurchaseDate() != null
				? LocalDate.parse(request.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				: null)
			.purchaseUrl(request.getPurchaseUrl())
			.description(request.getDescription())
			.isActive("Y")
			.createdBy(userId)
			.build();

		clothesRepository.insertClothes(clothes);

		for (MultipartFile file : request.getFiles()) {
			if (file != null && !file.isEmpty()) {
				FileInfo uploadedFile = fileService.uploadFile(file);

				ClothesImageSample clothesImage = ClothesImageSample.builder()
					.clothesId(clothesId)
					.fileId(uploadedFile.getFileId().longValue())
					.createdBy(userId)
					.build();

				int result = clothesRepository.insertClothesImage(clothesImage);
				if (result <= 0) {
					throw new RuntimeException("옷 이미지 등록 처리 중 오류가 발생했습니다.");
				}
			}
		}

		log.info("옷 등록 완료: clothesId={}", clothesId);
		return clothes;
	}

	@Override
	@Transactional
	@SaveHistory(entityType = "CLOTHES", actionType = "UPDATE")
	public ClothesSample updateClothes(String clothesId, ClothesRequestSample request, String userId) {
		log.info("옷 수정 시작: clothesId={}, userId={}", clothesId, userId);

		ClothesSample clothes = ClothesSample.builder()
			.clothesId(clothesId)
			.name(request.getName())
			.brand(request.getBrand())
			.categoryCode(request.getCategoryCode())
			.clothesSize(request.getClothesSize())
			.price(request.getPrice())
			.purchaseDate(request.getPurchaseDate() != null
				? LocalDate.parse(request.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				: null)
			.purchaseUrl(request.getPurchaseUrl())
			.description(request.getDescription())
			.updatedBy(userId)
			.build();

		clothesRepository.updateClothes(clothes);

		if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
			for (Long fileId : request.getDeletedFileIds()) {
				try {
					clothesRepository.deleteClothesImage(clothesId, fileId);
					log.info("이미지 삭제 완료: clothesId={}, fileId={}", clothesId, fileId);
				} catch (Exception e) {
					log.warn("이미지 삭제 실패: clothesId={}, fileId={}", clothesId, fileId, e);
				}
			}
		}

		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				if (file != null && !file.isEmpty()) {
					FileInfo uploadedFile = fileService.uploadFile(file);

					ClothesImageSample clothesImage = ClothesImageSample.builder()
						.clothesId(clothesId)
						.fileId(uploadedFile.getFileId().longValue())
						.createdBy(userId)
						.build();

					clothesRepository.insertClothesImage(clothesImage);
				}
			}
		}

		log.info("옷 수정 완료: clothesId={}", clothesId);
		return clothes;
	}

	@Override
	public List<ClothesResponseSample> getUserClothes(String userId) {
		log.info("옷 목록 조회: userId={}", userId);

		List<ClothesResponseSample> clothesList = clothesRepository.selectUserClothes(userId);

		log.info("옷 목록 조회 완료: count={}", clothesList.size());
		return clothesList;
	}

	@Override
	public ClothesDetailResponseDto getClothesDetail(String clothesId, String userId) {
		log.info("옷 상세 조회: clothesId={}, userId={}", clothesId, userId);

		ClothesDetailResponseDto clothes = clothesRepository.selectClothesById(clothesId, userId);
		if (clothes == null) {
			throw new IllegalArgumentException("옷 정보를 찾을 수 없습니다.");
		}

		List<ClothesDetailResponseDto.ClothesImage> images = clothesRepository.selectClothesImage(clothesId);
		clothes.setImages(images);

		log.info("옷 상세 조회 완료: clothesId={}", clothesId);
		return clothes;
	}

	private String generateClothesId() {
		int nextSeq = clothesRepository.getNextClothesSequence();
		return String.format("C%s%03d", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")), nextSeq);
	}
}
