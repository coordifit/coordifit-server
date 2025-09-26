package com.miracle.coordifit.clothes.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.miracle.coordifit.clothes.dto.ClothesCreateRequestDto;
import com.miracle.coordifit.clothes.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.dto.CodeDto;
import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.repository.ClothesMapper;
import com.miracle.coordifit.image.service.ImageService;

@Service
public class ClothesService {
	private static final int MIN_IMAGES = 1;
	private static final int MAX_IMAGES = 5;
	private static final long MAX_BYTES_PER_IMAGE = 10L * 1024 * 1024;
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png",
		"image/webp");

	private final ClothesMapper clothesMapper;
	private final ImageService imageService;
	private final Executor imageUploadExecutor;

	public ClothesService(ClothesMapper clothesMapper,
		ImageService imageService,
		Executor imageUploadExecutor) {
		this.clothesMapper = clothesMapper;
		this.imageService = imageService;
		this.imageUploadExecutor = imageUploadExecutor; // AsyncConfig bean
	}

	// ===== CRUD =====
	@Transactional
	public ClothesDetailResponseDto create(ClothesCreateRequestDto req, String loginUserId) {
		Objects.requireNonNull(loginUserId, "loginUserId");

		// 카테고리 존재 검증
		if (clothesMapper.selectOne(req.getCategoryCode()) == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"유효하지 않은 카테고리 코드: " + req.getCategoryCode());
		}

		Clothes clothes = Clothes.builder()
			.userId(loginUserId)
			.name(req.getName())
			.brand(req.getBrand())
			.categoryCode(req.getCategoryCode())
			.clothesSize(req.getClothesSize())
			.price(req.getPrice())
			.purchaseDate(req.getPurchaseDate())
			.purchaseUrl(req.getPurchaseUrl())
			.description(req.getDescription())
			.isActive("Y")
			.createdBy(loginUserId)
			.build();

		clothesMapper.insertClothes(clothes);
		return clothesMapper.selectClothesDetail(clothes.getClothesId());
	}

	@Transactional
	public ClothesDetailResponseDto createWithImages(ClothesCreateRequestDto req,
		String loginUserId,
		List<MultipartFile> files) throws IOException {
		var created = create(req, loginUserId);

		List<MultipartFile> validFiles = (files == null ? List.<MultipartFile>of() : files).stream()
			.filter(f -> f != null && !f.isEmpty())
			.collect(Collectors.toList());

		if (validFiles.size() < MIN_IMAGES || validFiles.size() > MAX_IMAGES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"이미지는 " + MIN_IMAGES + "장 이상 " + MAX_IMAGES + "장 이하로 업로드해야 합니다.");
		}
		for (MultipartFile f : validFiles) {
			String ct = f.getContentType();
			if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct.toLowerCase())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"허용되지 않는 이미지 형식: " + ct + " (허용: jpg, jpeg, png, webp)");
			}
			if (f.getSize() > MAX_BYTES_PER_IMAGE) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"이미지 한 장당 최대 10MB입니다: " + f.getOriginalFilename());
			}
		}

		// 업로드 전 FILE_INFO 최대값 스냅샷
		Long beforeMax = clothesMapper.selectMaxFileId();

		// 병렬 업로드
		List<CompletableFuture<Void>> futures = validFiles.stream()
			.map(f -> CompletableFuture.runAsync(() -> {
				try {
					imageService.uploadImage(f); // image 패키지는 변경하지 않음
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, imageUploadExecutor))
			.toList();

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// 새 FILE_ID만 조회해서 매핑
		var newFileIds = clothesMapper.selectFileIdsAfter(beforeMax);
		if (newFileIds == null || newFileIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 메타데이터 저장 실패");
		}
		clothesMapper.insertClothesImages(created.getClothesId(), newFileIds);

		return clothesMapper.selectClothesDetail(created.getClothesId());
	}

	public ClothesDetailResponseDto getDetail(String clothesId) {
		return clothesMapper.selectClothesDetail(clothesId);
	}

	public List<ClothesDetailResponseDto> getList(String userId, String categoryCode) {
		return clothesMapper.selectClothesList(userId, categoryCode);
	}

	// ===== 카테고리 조회 =====
	public List<CodeDto> getTopCategories() {
		return clothesMapper.selectTopCategories();
	}

	public List<CodeDto> getChildren(String parentCodeId) {
		return clothesMapper.selectChildren(parentCodeId);
	}

	public List<CodeDto> getTree() {
		var parents = clothesMapper.selectTopCategories();
		for (var p : parents)
			p.setChildren(clothesMapper.selectChildren(p.getCodeId()));
		return parents;
	}
}
