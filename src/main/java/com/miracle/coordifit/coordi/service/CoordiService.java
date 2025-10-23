package com.miracle.coordifit.coordi.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;
import com.miracle.coordifit.coordi.dto.CoordiResponse;
import com.miracle.coordifit.coordi.mapper.CoordiMapper;
import com.miracle.coordifit.coordi.model.Coordi;
import com.miracle.coordifit.coordi.model.CoordiItem;
import com.miracle.coordifit.coordi.repository.CoordiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordiService implements ICoordiService {
	private final CoordiMapper coordiMapper;
	private final CoordiRepository coordiRepository;
	private final IFileService fileService;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public List<CoordiResponse> getAllCoordisByUser(String userId) {
		try {
			log.info(">> getAllCoordisByUser called - userId={}", userId);

			if (userId == null || userId.isBlank()) {
				log.warn("⚠️ userId is null or blank");
				throw new IllegalArgumentException("사용자 정보가 유효하지 않습니다.");
			}

			List<Coordi> coordis = coordiRepository.getAllCoordisByUser(userId);
			if (coordis == null || coordis.isEmpty()) {
				log.info(">> No coordis found for userId={}", userId);
				return Collections.emptyList();
			}

			List<Integer> thumbIds = coordis.stream()
				.map(Coordi::getFileId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			log.info("thumbIds data {}", thumbIds.toString());

			Map<Integer, FileInfo> thumbMap = thumbIds.isEmpty()
				? Collections.emptyMap()
				: fileService.getFilesByIds(thumbIds);

			thumbMap.forEach((id, fileInfo) -> log.info("thumbMap[{}] = s3Url={}, thumbUrl={}",
				id, fileInfo.getS3Url(), fileInfo.getS3ThumbnailUrl()));
			List<CoordiResponse> responses = coordiMapper.toReponseList(coordis, thumbMap);

			log.info(">> getAllCoordisByUser success - userId={}, count={}", userId, responses.size());

			return responses;
		} catch (IllegalArgumentException e) {
			// 잘못된 파라미터
			log.warn("Invalid argument in getAllCoordisByUser: {}", e.getMessage());
			throw e; // 그대로 위로 올림 (ControllerAdvice에서 잡히게)
		} catch (DataAccessException e) {
			// DB 관련 예외 (MyBatis, JDBC 등)
			log.error("DB error while fetching coordis for userId={}", userId, e);
			throw new RuntimeException("코디 데이터를 조회하는 중 오류가 발생했습니다.", e);
		} catch (Exception e) {
			// 기타 예외
			log.error("Unexpected error in getAllCoordisByUser for userId={}", userId, e);
			throw new RuntimeException("코디 목록 조회 중 알 수 없는 오류가 발생했습니다.", e);
		}
	};

	@Override
	@Transactional
	public CoordiResponse getCoordiById(String coordiId) {
		Optional<Coordi> optional = coordiRepository.getCoordiById(coordiId);

		if (optional.isEmpty()) {
			return CoordiResponse.empty();
		}

		Coordi coordi = optional.get();

		log.info(">> coordi from getById, {}", coordi.toString());

		Integer fileId = coordi.getFileId();

		FileInfo imageInfo = fileService.getFileById(fileId);

		CoordiResponse response = coordiMapper.toResponse(coordi, imageInfo.getS3Url(), imageInfo.getS3ThumbnailUrl());

		return response;
	};

	@Override
	@Transactional
	public int upsertCoordi(Coordi coordi) {
		if (coordi.getCoordiId() == null || coordi.getCoordiId().isBlank()) {
			String coordiId = generateCoordiId();
			coordi.setCoordiId(coordiId);
			log.info(">>>>> [INSERT] coordi: {}", coordi);
			int result = coordiRepository.insertCoordi(coordi);

			return result;
		} else {
			log.info(">>>>> [UPDATE] coordi: {}", coordi);
			int result = coordiRepository.updateCoordiById(coordi);

			return result;
		}
	};

	@Override
	@Transactional
	public void insertCoordiItem(String canvasJson, Coordi coordi) {
		List<CoordiItem> itemList = parseCanvasJson(canvasJson, coordi);

		log.info(">> parese Coordi CanvasJson: {}", itemList.toString());

		for (CoordiItem coordiItem : itemList) {
			coordiRepository.insertCoordiItem(coordiItem);
		}
	}

	@Override
	@Transactional
	public void deleteCoordi(String coordiId) {
		Optional<Coordi> optional = coordiRepository.getCoordiById(coordiId);
		if (optional.isEmpty()) {
			throw new RuntimeException("삭제하려는 코디가 존재하지 않습니다. ID: " + coordiId);
		}

		Coordi coordi = optional.get();
		log.info(">>>>> deleteCoordi - target: {}", coordi.toString());

		try {
			int deletedItems = coordiRepository.deleteCoordiItemsByLookId(coordiId);
			log.info(">>>>> deleted {} coordi items for {}", deletedItems, coordiId);
		} catch (Exception e) {
			log.warn(">>>>> LOOK_ITEMS 삭제 중 예외 발생 (CASCADE 설정일 가능성): {}", e.getMessage());
		}

		int deletedCoordi = coordiRepository.deleteCoordiById(coordiId);
		log.info(">>>>> deleted coordi {}, result={}", coordiId, deletedCoordi);

		if (deletedCoordi == 0) {
			throw new RuntimeException("코디 삭제 실패: " + coordiId);
		}

		log.info(">>>>> deleteCoordi 완료: {}", coordiId);
	}

	@Override
	@Transactional
	public void deleteCoordis(List<String> coordiIds) {
		if (coordiIds == null || coordiIds.isEmpty()) {
			throw new IllegalArgumentException("삭제할 코디 ID 목록이 비어 있습니다.");
		}

		log.info(">>>>> deleteCoordis - 요청된 ID 개수: {}", coordiIds.size());

		for (String coordiId : coordiIds) {
			try {
				deleteCoordi(coordiId); // 기존 단일 삭제 메서드 재사용
			} catch (Exception e) {
				log.error(">>>>> deleteCoordi 실패 (coordiId={}): {}", coordiId, e.getMessage());
			}
		}

		log.info(">>>>> deleteCoordis 완료 - 총 {}개 요청 처리", coordiIds.size());
	}

	private List<CoordiItem> parseCanvasJson(String canvasJson, Coordi coordi) {
		try {
			List<Map<String, Object>> rawList = objectMapper.readValue(canvasJson,
				new TypeReference<List<Map<String, Object>>>() {});

			return rawList.stream().map(object -> {
				CoordiItem coordiItem = new CoordiItem();

				coordiItem.setUserId(coordi.getUserId());
				coordiItem.setCoordiId(coordi.getCoordiId());
				coordiItem.setUserId(coordi.getUserId());
				coordiItem.setClothesId((String)object.get("clothesId"));

				return coordiItem;
			}).toList();
		} catch (Exception e) {
			throw new RuntimeSqlException("canvasItems 파싱 실패", e);
		}
	}

	private String generateCoordiId() {
		DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
		String date = LocalDate.now().format(YYMMDD);
		int nextSeq = coordiRepository.getNextCoordiSequence();

		return String.format("L%s%03d", date, nextSeq);
	}
}
