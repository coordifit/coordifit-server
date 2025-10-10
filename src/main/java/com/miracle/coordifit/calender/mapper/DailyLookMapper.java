package com.miracle.coordifit.calender.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.miracle.coordifit.calender.dto.DailyLookResponse;
import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.common.model.FileInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyLookMapper {

	public DailyLookResponse toResponse(DailyLook dailyLook, String originImageUrl, String thumbImageUrl) {
		if (dailyLook == null)
			return null;

		return DailyLookResponse.builder()
			.dailylookId(dailyLook.getDailylookId())
			.userId(dailyLook.getUserId())
			.wearDate(dailyLook.getWearDate())
			.description(dailyLook.getDescription())
			.canvasJson(dailyLook.getCanvasJson())
			.originImageUrl(originImageUrl)
			.thumbImageUrl(thumbImageUrl)
			.build();
	}

	public List<DailyLookResponse> toResponseList(List<DailyLook> dailyLooks, Map<Integer, FileInfo> thumbMap) {
		return dailyLooks.stream()
			.map(dailyLook -> {
				FileInfo fileInfo = thumbMap.get(dailyLook.getThumbImageId());

				return toResponse(dailyLook, null, fileInfo.getS3Url());
			})
			.collect(Collectors.toList());
	}
}
