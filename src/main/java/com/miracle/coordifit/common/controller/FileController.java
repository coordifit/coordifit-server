package com.miracle.coordifit.common.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
	private final FileService fileService;

	@PostMapping
	public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(fileService.uploadFile(file));
	}

	@GetMapping
	public ResponseEntity<List<FileInfo>> getFiles() {
		List<FileInfo> files = fileService.getFiles();
		return ResponseEntity.ok(files);
	}

	@GetMapping("/{fileId}")
	public ResponseEntity<FileInfo> getFileById(@PathVariable("fileId") Integer fileId) {
		FileInfo fileInfo = fileService.getFileById(fileId);
		if (fileInfo == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(fileInfo);
	}
}
