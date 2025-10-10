package com.miracle.coordifit.calender.service;

import java.util.List;

import com.miracle.coordifit.calender.dto.DailyLookResponse;
import com.miracle.coordifit.calender.model.DailyLook;

public interface ICalenderService {
	// 데일리룩 저장
	int upsertDailyLook(DailyLook dailyLook);

	// 데일리룩에 사용된 아이템 저장
	void insertDailyLookItem(String itemsJson, DailyLook dailyLook);

	// 특정 월 데일리룩 조회
	List<DailyLookResponse> getDailyLooksByMonth(String userId, String yearMonth);

	// 특정 날짜 데일리룩 조회
	DailyLookResponse getDailyLookByDate(String userId, String wearDate);
}
