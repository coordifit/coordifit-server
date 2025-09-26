package com.miracle.coordifit.common.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService implements IFileService {
	private final S3Service s3Service;
	private final FileRepository fileRepository;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Override
	@Transactional
	public FileInfo uploadFile(MultipartFile file) {
		try {
			String url = s3Service.uploadFile(file);
			String fileName = url.substring(url.lastIndexOf("/") + 1);

			FileInfo fileInfo = FileInfo.builder()
				.originalName(file.getOriginalFilename())
				.s3Key(fileName)
				.s3Url(url)
				.bucketName(bucketName)
				.fileSize(file.getSize())
				.fileType(file.getContentType())
				.uploadBy("ADMIN")
				.build();

			fileRepository.insertFileInfo(fileInfo);
			return fileInfo;
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public FileInfo getFileById(Integer fileId) {
		return fileRepository.selectFileInfoById(fileId);
	}

	@Override
	public List<FileInfo> getFiles() {
		return fileRepository.selectFileInfos();
	}
}
