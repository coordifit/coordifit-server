package com.miracle.coordifit.common.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.common.model.CommonCode;
import com.miracle.coordifit.common.service.CommonCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {
	private final CommonCodeService commonCodeService;

	@GetMapping
	public Map<String, CommonCode> getCommonCodes() {
		return commonCodeService.getCommonCodes();
	}

	@PostMapping
	public CommonCode createCommonCode(@RequestBody
	CommonCode commonCode) {
		return commonCodeService.createCommonCode(commonCode);
	}

	@PutMapping("/{codeId}")
	public CommonCode updateCommonCode(@PathVariable("codeId")
	String codeId, @RequestBody
	CommonCode commonCode) {
		commonCode.setCodeId(codeId);
		return commonCodeService.updateCommonCode(commonCode);
	}

	@DeleteMapping("/{codeId}")
	public ResponseEntity<Void> deleteCommonCode(@PathVariable("codeId")
	String codeId) {
		commonCodeService.deleteCommonCode(codeId);
		return ResponseEntity.ok().build();
	}
}
