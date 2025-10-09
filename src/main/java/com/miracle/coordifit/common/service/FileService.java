package com.miracle.coordifit.common.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

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

	@Value("${file.thumbnail.suffix}")
	private String thumbnailSuffix;

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

	/**
	 * For Saving Thumbnail Image File Using uploadFile
	 * @param MultipartFile Save target image file.
	 * @return FileInfo Fileinfo model
	 */
	@Override
	@Transactional
	public FileInfo uploadThumbnail(MultipartFile file) {

		try {
			// transform MultipartFile to BufferedImage
			BufferedImage originalImage = ImageIO.read(file.getInputStream());

			// create thumbnail image (300 * 300)
			BufferedImage thumbnail = Thumbnails.of(originalImage).size(300, 300).keepAspectRatio(true)
				.asBufferedImage();

			// write thumbnail image to ByteArrayOutputStream
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(thumbnail, "png", baos);

			// transform ByteArrayOutputStream to MultipartFile
			MultipartFile thumbnailFile = new MockMultipartFile(
				file.getOriginalFilename() + thumbnailSuffix,
				file.getOriginalFilename() + thumbnailSuffix,
				"image/png",
				baos.toByteArray());

			// upload image to S3 and save metadata to DB
			return uploadFile(thumbnailFile);
		} catch (IOException e) {
			throw new RuntimeException("썸네일 생성 실패", e);
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
