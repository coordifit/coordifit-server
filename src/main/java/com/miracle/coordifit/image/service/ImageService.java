package com.miracle.coordifit.image.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.image.model.Image;
import com.miracle.coordifit.image.repository.ImageMapper;
import com.miracle.coordifit.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final ImageMapper imageMapper;
	private final S3Service s3Service;

	public void uploadImage(MultipartFile file) throws IOException {
		String url = s3Service.uploadFile(file);

		Image image = new Image();
		image.setFileName(file.getOriginalFilename());
		image.setUrl(url);

		imageMapper.insertImage(image);
	}

	public List<Image> getAllImages() {
		return imageMapper.selectAllImages();
	}
}
