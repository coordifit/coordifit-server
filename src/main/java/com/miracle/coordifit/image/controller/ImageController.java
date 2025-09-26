package com.miracle.coordifit.image.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.image.model.Image;
import com.miracle.coordifit.image.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
	private final ImageService imageService;

	@PostMapping
	public ResponseEntity<String> uploadImage(@RequestParam("file")
	MultipartFile file) {
		try {
			imageService.uploadImage(file);
			return ResponseEntity.ok("Image uploaded successfully");
		} catch (IOException e) {
			return ResponseEntity.internalServerError()
				.body("Image upload failed: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<Image>> getAllImages() {
		return ResponseEntity.ok(imageService.getAllImages());
	}
}
