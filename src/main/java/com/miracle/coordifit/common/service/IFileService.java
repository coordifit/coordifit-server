package com.miracle.coordifit.common.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.model.FileInfo;

public interface IFileService {
	FileInfo uploadFile(MultipartFile file);

	FileInfo getFileById(Integer fileId);

	List<FileInfo> getFiles();
}
