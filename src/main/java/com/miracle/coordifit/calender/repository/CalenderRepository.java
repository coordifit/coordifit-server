package com.miracle.coordifit.calender.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.calender.model.DailyLookItem;

@Mapper
public interface CalenderRepository {
	// 데일리룩 저장
	int insertDailyLook(DailyLook dailyLook);

	int insertDailyLookItem(DailyLookItem item);

	// 특정 월 데일리룩 조회
	List<DailyLook> getDailyLooksByMonth(
		@Param("userId") String userId,
		@Param("yearMonth") String yearMonth);

	// 특정 날짜 데일리룩 조회
	DailyLook getDailyLookByDate(
		@Param("userId") String userId,
		@Param("wearDate") String wearDate);

	// 데일리룩 수정
	int updateDailyLook(DailyLook dailyLook);
}
