package com.miracle.coordifit.fitting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.fitting.dto.FittingAnalysisErrorCode;
import com.miracle.coordifit.fitting.dto.FittingAnalysisErrorResponse;
import com.miracle.coordifit.fitting.dto.FittingAnalysisRequest;
import com.miracle.coordifit.fitting.dto.FittingAnalysisResponse;
import com.miracle.coordifit.fitting.service.FittingAnalysisService;
import com.miracle.coordifit.fitting.service.FittingAnalysisService.FittingAnalysisFailure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/fitting")
@RequiredArgsConstructor
public class FittingAnalysisController {

	private final FittingAnalysisService fittingAnalysisService;

	@PostMapping("/analysis")
	public ResponseEntity<?> analyze(@RequestBody FittingAnalysisRequest request) {
		try {
			FittingAnalysisResponse response = fittingAnalysisService.analyze(request);
			return ResponseEntity.ok(response);
		} catch (FittingAnalysisFailure e) {
			FittingAnalysisErrorCode errorCode = e.getErrorCode();
			FittingAnalysisErrorResponse errorResponse = FittingAnalysisErrorResponse.builder()
				.code(errorCode.getCode())
				.message(e.getMessage())
				.status(errorCode.getStatus().value())
				.build();
			return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
		} catch (Exception e) {
			log.error("피팅 분석 처리 중 알 수 없는 오류", e);
			FittingAnalysisErrorResponse errorResponse = FittingAnalysisErrorResponse.builder()
				.code(FittingAnalysisErrorCode.INTERNAL_ERROR.getCode())
				.message("피팅 분석 처리 중 오류가 발생했습니다.")
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
}
