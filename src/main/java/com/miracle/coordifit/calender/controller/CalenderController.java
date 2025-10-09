package com.miracle.coordifit.calender.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.calender.model.DailyLook;
import com.miracle.coordifit.calender.service.ICalenderService;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.FileService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/daily-look")
@RequiredArgsConstructor
@Slf4j
public class CalenderController {
	private final ICalenderService calenderService;
	private final FileService fileService;

	@GetMapping("/date")
	public ResponseEntity<?> getDailyLooks(
		@RequestParam(required = false) String yearMonth,
		@RequestParam(required = false) String wearDate,
		HttpSession session, Authentication authentication) {
		log.info("authentication: {}", authentication.toString());
		log.info("session: {}", session.getAttribute("userId"));

		String userId = (String)session.getAttribute("userId");

		if (wearDate != null) {
			// 특정 날짜 조회
			DailyLook dailyLook = calenderService.getDailyLookByDate(userId, wearDate);
			return ResponseEntity.ok(dailyLook);
		} else if (yearMonth != null) {
			// 월별 조회
			List<DailyLook> monthlyDailyLooks = calenderService.getDailyLooksByMonth(userId, yearMonth);
			return ResponseEntity.ok(monthlyDailyLooks);
		}

		return ResponseEntity.badRequest().body("yearMonth 또는 wearDate 파라미터를 제공해야 합니다.");
	}

	@Transactional
	@PostMapping("/date/{wearDate:\\d{4}-\\d{2}-\\d{2}}")
	public ResponseEntity<?> createDailyLook(
		@PathVariable("wearDate") String wearDate,
		@RequestPart("image") MultipartFile image,
		@RequestParam("description") String description,
		@RequestPart("items") String itemsJson,
		Authentication authentication) {
		String userId = (String)authentication.getPrincipal();

		FileInfo originImage = fileService.uploadFile(image);
		FileInfo thumbImage = fileService.uploadThumbnail(image);

		DailyLook dailyLook = new DailyLook();
		dailyLook.setUserId(userId);
		dailyLook.setWearDate(wearDate);
		dailyLook.setDescription(description);
		dailyLook.setOriginImageId(originImage.getFileId());
		dailyLook.setThumbImageId(thumbImage.getFileId());
		dailyLook.setCanvasJson(itemsJson);

		log.info("dailyLook info: {}", dailyLook.toString());

		int result = calenderService.insertDailyLook(dailyLook);

		calenderService.insertDailyLookItem(itemsJson, dailyLook);

		return ResponseEntity.ok(result);
	}
}
