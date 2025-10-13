package com.miracle.coordifit.common.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.dto.Base64ImageDto;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.IFileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

	private final IFileService fileService; // ✔ 인터페이스로 주입

	// --- Multipart 단건 (dev 유지) ---
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(fileService.uploadFile(file));
	}

	@PostMapping("/thumbnails")
	public ResponseEntity<FileInfo> uploadThumbnail(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(fileService.uploadThumbnail(file));
	}

	@GetMapping
	public ResponseEntity<List<FileInfo>> getFiles() {
		List<FileInfo> files = fileService.getFiles();
		return ResponseEntity.ok(files);
	}

	// --- 단건 조회 (dev 유지) ---
	@GetMapping("/{fileId}")
	public ResponseEntity<FileInfo> getFileById(@PathVariable Integer fileId) {
		FileInfo fileInfo = fileService.getFileById(fileId);
		return (fileInfo == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(fileInfo);
	}

	// --- Base64 단건 (신규) ---
	@PostMapping(value = "/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FileInfo> uploadBase64(@Valid @RequestBody Base64ImageDto dto) {
		return ResponseEntity.ok(fileService.uploadBase64(dto));
	}

	// --- Base64 배치 (신규) ---
	@PostMapping(value = "/base64/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<FileInfo>> uploadBase64Batch(@Valid @RequestBody List<Base64ImageDto> list) {
		return ResponseEntity.ok(fileService.uploadBase64Batch(list));
	}

}
