package com.miracle.coordifit.coordi.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.coordi.dto.CoordiResponse;
import com.miracle.coordifit.coordi.model.Coordi;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoordiMapper {

	public CoordiResponse toResponse(Coordi coordi, String originImageUrl, String thumbImageUrl) {
		if (coordi == null)
			return null;

		return CoordiResponse.builder()
			.coordiId(coordi.getCoordiId())
			.userId(coordi.getUserId())
			.title(coordi.getTitle())
			.description(coordi.getDescription())
			.canvasJson(coordi.getCanvasJson())
			.originImageUrl(originImageUrl)
			.thumbImageUrl(thumbImageUrl)
			.build();
	}

	public List<CoordiResponse> toReponseList(List<Coordi> coordis, Map<Integer, FileInfo> thumbMap) {
		List<CoordiResponse> resultList = coordis.stream()
			.map(coordi -> {
				FileInfo fileInfo = thumbMap.get(coordi.getThumbImageId());

				CoordiResponse coordiResponse = toResponse(coordi, null, fileInfo.getS3Url());

				return coordiResponse;
			})
			.collect(Collectors.toList());

		return resultList;
	}
}
