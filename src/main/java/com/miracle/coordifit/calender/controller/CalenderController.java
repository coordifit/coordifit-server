package com.miracle.coordifit.calender.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.calender.dto.DailyLookResponse;
import com.miracle.coordifit.calender.dto.DailyLookSummaryResponse;
import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.calender.service.ICalenderService;
import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/daily-look")
@RequiredArgsConstructor
@Slf4j
public class CalenderController {
	private final ICalenderService calenderService;
	private final FileService fileService;

	@Transactional
	@GetMapping("/date")
	public ResponseEntity<ApiResponseDto<?>> getDailyLooks(
		@RequestParam(value = "yearMonth", required = false) String yearMonth,
		@RequestParam(value = "wearDate", required = false) String wearDate,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		if (wearDate != null) {
			// 특정 날짜 조회
			DailyLookResponse dailyLook = calenderService.getDailyLookByDate(userId, wearDate);

			return ResponseEntity.ok()
				.body(ApiResponseDto.success(String.format("%s 날짜의 데일리룩 데이터 조회 성공", wearDate), dailyLook));
		} else if (yearMonth != null) {
			// 월별 조회
			List<DailyLookResponse> monthlyDailyLooks = calenderService.getDailyLooksByMonth(userId, yearMonth);

			return ResponseEntity.ok()
				.body(ApiResponseDto.success(String.format("%s 월별 데일리룩 데이터 조회 성공", yearMonth), monthlyDailyLooks));
		}

		return ResponseEntity.badRequest()
			.body(ApiResponseDto.error("yearMonth 또는 wearDate 파라미터를 제공해야 합니다."));
	}

	@GetMapping("/summary")
	public ResponseEntity<ApiResponseDto<?>> getStatistics(@RequestParam String yearMonth,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		DailyLookSummaryResponse data = calenderService.getDailyLookSummary(userId, yearMonth);

		return ResponseEntity.ok(ApiResponseDto.success("월간 통계 조회 성공", data));
	}

	@DeleteMapping("/date/{wearDate:\\d{4}-\\d{2}-\\d{2}}")
	@Transactional
	public ResponseEntity<ApiResponseDto<?>> deleteDailyLook(
		@PathVariable("wearDate") String wearDate,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		try {
			int deletedCount = calenderService.deleteDailyLookByDate(userId, wearDate);

			if (deletedCount > 0) {
				return ResponseEntity.ok()
					.body(ApiResponseDto.success(String.format("%s 날짜의 데일리룩 삭제 성공", wearDate), null));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponseDto.error(String.format("%s 날짜의 데일리룩을 찾을 수 없습니다.", wearDate)));
			}

		} catch (Exception e) {
			log.error("❌ 데일리룩 삭제 중 오류 발생: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("데일리룩 삭제 중 오류가 발생했습니다."));
		}
	}

	@Transactional
	@PostMapping("/date/{wearDate:\\d{4}-\\d{2}-\\d{2}}")
	public ResponseEntity<ApiResponseDto<?>> createDailyLook(
		@PathVariable("wearDate") String wearDate,
		@RequestPart("image") MultipartFile image,
		@RequestParam("description") String description,
		@RequestPart("items") String itemsJson,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		FileInfo imageInfo = fileService.uploadFileWithThumbnail(image);

		DailyLook dailyLook = DailyLook.builder()
			.userId(userId)
			.wearDate(wearDate)
			.description(description)
			.fileId(imageInfo.getFileId())
			.canvasJson(itemsJson)
			.build();

		log.info(">> Before insert dailyLook info: {}", dailyLook.toString());
		try {
			int result = calenderService.upsertDailyLook(dailyLook);

			log.info(">> calendar editor post coordi save success {}", result);

			if (dailyLook.getDailylookId() == null) {
				throw new IllegalStateException("dailyLookId가 설정되지 않았습니다.");
			}

			calenderService.insertDailyLookItem(itemsJson, dailyLook);

			log.info(">> calendar editor post dailylook items save success");

			URI location = URI.create("/api/daily-look/date/" + dailyLook.getWearDate());

			Map<String, Object> body = Map.of("dailyLookId", dailyLook.getDailylookId(), "affectedRows", result);

			return ResponseEntity.created(location).body(ApiResponseDto.success("데일리룩 등록 성공", body));
		} catch (Exception e) {
			log.error(">> create DailyLook failed", e);
			return ResponseEntity.internalServerError()
				.body(ApiResponseDto.error("데일리룩 저장 실패", e.getMessage()));
		}
	}
}
