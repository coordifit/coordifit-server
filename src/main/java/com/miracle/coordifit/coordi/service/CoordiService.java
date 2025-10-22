package com.miracle.coordifit.coordi.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.jdbc.RuntimeSqlException;
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
		List<Coordi> coordis = coordiRepository.getAllCoordisByUser(userId);

		List<Integer> thumbIds = coordis.stream()
			.map(Coordi::getThumbImageId)
			.filter(Objects::nonNull)
			.toList();

		Map<Integer, FileInfo> thumbMap = fileService.getFilesByIds(thumbIds);

		return coordiMapper.toReponseList(coordis, thumbMap);
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

		Integer originImageId = coordi.getOriginImageId();

		FileInfo fileInfo = fileService.getFileById(originImageId);
		String originImageUrl = fileInfo.getS3Url();

		CoordiResponse response = coordiMapper.toResponse(coordi, originImageUrl, null);

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
