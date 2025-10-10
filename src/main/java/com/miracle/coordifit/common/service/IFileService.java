package com.miracle.coordifit.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.model.FileInfo;

public interface IFileService {
	FileInfo uploadFile(MultipartFile file);

	FileInfo uploadThumbnail(MultipartFile file);

	FileInfo getFileById(Integer fileId);

	Map<Integer, FileInfo> getFilesByIds(List<Integer> fileIds);

	List<FileInfo> getFiles();
}
