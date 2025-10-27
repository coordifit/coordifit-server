package com.miracle.coordifit.calender.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miracle.coordifit.calender.dto.DailyLookResponse;
import com.miracle.coordifit.calender.dto.DailyLookSummaryResponse;
import com.miracle.coordifit.calender.mapper.DailyLookMapper;
import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.calender.model.DailyLookItem;
import com.miracle.coordifit.calender.model.MostWornClothesDto;
import com.miracle.coordifit.calender.repository.CalenderRepository;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalenderService implements ICalenderService {
	private final CalenderRepository calenderRepository;
	private final DailyLookMapper dailyLookMapper;
	private final IFileService fileservice;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public int deleteDailyLookByDate(String userId, String wearDate) {
		Optional<DailyLook> existing = calenderRepository.getDailyLookByDate(userId, wearDate);
		if (existing.isEmpty()) {
			return 0;
		}

		DailyLook target = existing.get();

		int deleted = calenderRepository.deleteDailyLookById(target.getDailylookId());

		return deleted;
	}

	@Override
	@Transactional
	public int upsertDailyLook(DailyLook dailyLook) {

		Optional<DailyLook> existing = calenderRepository.getDailyLookByDate(dailyLook.getUserId(),
			dailyLook.getWearDate());

		if (existing.isPresent()) {
			DailyLook exist = existing.get();

			dailyLook.setDailylookId(exist.getDailylookId());

			log.info(">>>>> dailyLook in update {}", dailyLook.toString());
			return calenderRepository.updateDailyLook(dailyLook);
		} else {
			String dailylookId = generateDailylookId();

			dailyLook.setDailylookId(dailylookId);

			log.info(">>>>> dailyLook in insert {}", dailyLook.toString());
			return calenderRepository.insertDailyLook(dailyLook);
		}
	}

	@Override
	@Transactional
	public void insertDailyLookItem(String itemsJson, DailyLook dailyLook) {
		List<DailyLookItem> items = parseItemsJson(itemsJson, dailyLook);
		log.info("parsed items : {}", items);

		for (DailyLookItem item : items) {
			try {
				calenderRepository.insertDailyLookItem(item);
			} catch (DuplicateKeyException e) {
				log.warn("이미 등록된 아이템: dailylookId={}, clothesId={}",
					item.getDailylookId(), item.getClothesId());
			}
		}
	}

	@Override
	public List<DailyLookResponse> getDailyLooksByMonth(String userId, String yearMonth) {
		List<DailyLook> dailyLooks = calenderRepository.getDailyLooksByMonth(userId, yearMonth);

		List<Integer> fileIdList = dailyLooks.stream()
			.map(DailyLook::getFileId)
			.filter(Objects::nonNull)
			.toList();

		Map<Integer, FileInfo> thumbMap = fileservice.getFilesByIds(fileIdList);

		return dailyLookMapper.toResponseList(dailyLooks, thumbMap);
	}

	@Override
	public DailyLookResponse getDailyLookByDate(String userId, String wearDate) {
		Optional<DailyLook> optional = calenderRepository.getDailyLookByDate(userId, wearDate);

		if (optional.isEmpty()) {
			return DailyLookResponse.empty();
		}

		DailyLook dailyLook = optional.get();

		log.info("dailyLook : {}", dailyLook.toString());
		Integer imageId = dailyLook.getFileId();

		FileInfo fileInfo = fileservice.getFileById(imageId);
		String originImageUrl = fileInfo.getS3Url();
		String thumbImageUrl = fileInfo.getS3ThumbnailUrl();

		DailyLookResponse response = dailyLookMapper.toResponse(dailyLook, originImageUrl, thumbImageUrl);

		return response;
	}

	@Override
	public DailyLookSummaryResponse getDailyLookSummary(String userId, String yearMonth) {
		int totalCount = calenderRepository.selectDailyLookCountByMonth(userId, yearMonth);
		MostWornClothesDto mostWornClothes = calenderRepository.selectMostWornClothes(userId);
		MostWornClothesDto mostWornClothesByMonth = calenderRepository.selectMostWornClothesByMonth(userId, yearMonth);

		if (mostWornClothes == null)
			mostWornClothes = MostWornClothesDto.empty();
		if (mostWornClothesByMonth == null)
			mostWornClothesByMonth = MostWornClothesDto.empty();

		return DailyLookSummaryResponse.builder()
			.totalDailyLookCount(totalCount)
			.mostWornClothesOverall(mostWornClothes)
			.mostWornClothesThisMonth(mostWornClothesByMonth)
			.build();
	}

	private List<DailyLookItem> parseItemsJson(String itemsJson, DailyLook dailyLook) {
		try {
			List<Map<String, Object>> rawList = objectMapper.readValue(itemsJson,
				new TypeReference<List<Map<String, Object>>>() {});

			return rawList.stream().map(obj -> {
				DailyLookItem item = new DailyLookItem();
				item.setUserId(dailyLook.getUserId());
				item.setDailylookId(dailyLook.getDailylookId());
				item.setWearDate(Date.valueOf(dailyLook.getWearDate()));
				item.setClothesId((String)obj.get("clothesId"));

				return item;
			}).toList();
		} catch (Exception e) {
			throw new RuntimeException("itemsJson 파싱 실패", e);
		}
	}

	private String generateDailylookId() {
		DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
		String date = LocalDate.now().format(YYMMDD);
		int nextSeq = calenderRepository.getNextDailyLookSequence();

		return String.format("D%s%03d", date, nextSeq);
	}
}
