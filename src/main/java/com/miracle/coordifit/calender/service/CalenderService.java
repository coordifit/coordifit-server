package com.miracle.coordifit.calender.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.calender.model.DailyLookItem;
import com.miracle.coordifit.calender.repository.CalenderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalenderService implements ICalenderService {
	private final CalenderRepository calenderRepository;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public int insertDailyLook(DailyLook dailyLook) {
		return calenderRepository.insertDailyLook(dailyLook);
	}

	@Override
	@Transactional
	public void insertDailyLookItem(String itemsJson, DailyLook dailyLook) {
		List<DailyLookItem> items = parseItemsJson(itemsJson, dailyLook);
		log.info("parsed items : {}", items.toString());

		for (DailyLookItem item : items) {
			calenderRepository.insertDailyLookItem(item);
		}
	}

	@Override
	public List<DailyLook> getDailyLooksByMonth(String userId, String yearMonth) {
		return calenderRepository.getDailyLooksByMonth(userId, yearMonth);
	}

	@Override
	public DailyLook getDailyLookByDate(String userId, String wearDate) {
		return calenderRepository.getDailyLookByDate(userId, wearDate);
	}

	@Override
	public int updateDailyLook(DailyLook dailyLook) {
		return calenderRepository.updateDailyLook(dailyLook);
	}

	private List<DailyLookItem> parseItemsJson(String itemsJson, DailyLook dailyLook) {
		try {
			List<Map<String, Object>> rawList = objectMapper.readValue(itemsJson,
				new TypeReference<List<Map<String, Object>>>() {});

			return rawList.stream().map(obj -> {
				DailyLookItem item = new DailyLookItem();
				item.setUserId(dailyLook.getUserId());
				item.setDailyLookId(dailyLook.getDailyLookId());
				item.setWearDate(Date.valueOf(dailyLook.getWearDate()));
				item.setClothesId((String)obj.get("id"));

				return item;
			}).toList();
		} catch (Exception e) {
			throw new RuntimeException("itemsJson 파싱 실패", e);
		}
	}
}
