package com.miracle.coordifit.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.dto.Base64ImageDto;
import com.miracle.coordifit.common.model.FileInfo;

public interface IFileService {
	// Multipart 업로드
	FileInfo uploadFile(MultipartFile file);

	// 썸네일 업로드
	FileInfo uploadThumbnail(MultipartFile file);

	// 조회
	FileInfo getFileById(Integer fileId);

	Map<Integer, FileInfo> getFilesByIds(List<Integer> fileIds);

	List<FileInfo> getFiles();

	// Base64 단건/배치 업로드
	FileInfo uploadBase64(Base64ImageDto dto);

	List<FileInfo> uploadBase64Batch(List<Base64ImageDto> list);

	void deleteFileById(Long fileId);
}
