package com.miracle.coordifit.common.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import com.miracle.coordifit.common.dto.Base64ImageDto;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements IFileService {
	private final S3Service s3Service;
	private final FileRepository fileRepository;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Value("${file.thumbnail.suffix}")
	private String thumbnailSuffix;

	private static final Pattern BASE64_ALLOWED_BODY = Pattern.compile("^[A-Za-z0-9+/=]+$");

	/** data: 접두 제거 + 공백/개행 제거 + URL-safe(-,_)→표준(+,/) + 패딩 재계산(최대 2개) + 따옴표 잔재 제거 */
	private static String sanitizeBase64(String dataUrlOrRaw) {
		if (dataUrlOrRaw == null)
			return null;
		String s = dataUrlOrRaw.trim();

		// 따옴표/백틱 잔재 제거
		if ((s.startsWith("\"") && s.endsWith("\"")) ||
			(s.startsWith("'") && s.endsWith("'")) ||
			(s.startsWith("`") && s.endsWith("`"))) {
			s = s.substring(1, s.length() - 1);
		}

		// data URL 접두 제거
		int comma = s.indexOf(',');
		if (s.startsWith("data:") && comma >= 0) {
			s = s.substring(comma + 1);
		}

		// 공백/개행 제거
		s = s.replaceAll("\\s+", "");

		// URL-safe → 표준
		s = s.replace('-', '+').replace('_', '/');

		// 패딩 정규화:
		// 1) 끝의 '=' 전부 제거
		int eqRun = 0;
		for (int i = s.length() - 1; i >= 0 && s.charAt(i) == '='; i--)
			eqRun++;
		if (eqRun > 0)
			s = s.substring(0, s.length() - eqRun);

		// 2) 중간에 '=' 가 끼어있으면 잘못된 문자열
		int midEq = s.indexOf('=');
		if (midEq >= 0) {
			throw new IllegalArgumentException("잘못된 위치의 '='(패딩은 끝에만 허용)");
		}

		// 3) 길이 % 4에 맞춰 정확히 재부착
		int rem = s.length() % 4;
		if (rem == 0) {
			// no-op
		} else if (rem == 2) {
			s = s + "==";
		} else if (rem == 3) {
			s = s + "=";
		} else { // rem == 1 은 정상적인 Base64가 될 수 없음
			throw new IllegalArgumentException("잘못된 Base64 길이(mod 1)");
		}

		return s;
	}

	public static byte[] decodeBase64SafeForPreflight(String dataUrlOrRaw) {
		return decodeBase64SafeImpl(dataUrlOrRaw, true);
	}

	public static byte[] decodeBase64Safe(String dataUrlOrRaw) {
		return decodeBase64SafeImpl(dataUrlOrRaw, false);
	}

	private static byte[] decodeBase64SafeImpl(String dataUrlOrRaw, boolean verbose) {
		String cleaned = sanitizeBase64(dataUrlOrRaw);
		if (cleaned == null || cleaned.isBlank()) {
			throw new IllegalArgumentException("Base64 비어있음");
		}

		// 허용문자 검사 (A-Z a-z 0-9 + / =) — sanitize 후라 내부 '=' 는 끝 패딩에만 있음
		if (!BASE64_ALLOWED_BODY.matcher(cleaned).matches()) {
			String bad = cleaned.replaceAll("[A-Za-z0-9+/=]", "");
			String sample = bad.substring(0, Math.min(20, bad.length()));
			throw new IllegalArgumentException("허용되지 않는 문자: '" + sample + "'");
		}

		try {
			return java.util.Base64.getMimeDecoder().decode(cleaned);
		} catch (IllegalArgumentException e) {
			int len = cleaned.length();
			int mod = len % 4;
			String head = cleaned.substring(0, Math.min(30, len));
			String tail = cleaned.substring(Math.max(0, len - 30));
			if (verbose) {
				log.error("Base64 decode failed head='{}' tail='{}' len={} mod={}", head, tail, len, mod);
			}
			throw new IllegalArgumentException("Base64 디코딩 실패(length=" + len + ", mod4=" + mod + ")", e);
		}
	}

	/** 간이 Content-Type 추정 (시그니처 기반) */
	private static String guessContentType(byte[] bytes, String fallback) {
		if (bytes == null || bytes.length < 4)
			return fallback;
		// PNG: 89 50 4E 47
		if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
			return "image/png";
		}
		// JPG: FF D8
		if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
			return "image/jpeg";
		}
		// GIF: 47 49 46
		if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
			return "image/gif";
		}
		// WEBP: "RIFF....WEBP"
		if (bytes.length >= 12 &&
			bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
			bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
			return "image/webp";
		}
		return (fallback != null && !fallback.isBlank()) ? fallback : "application/octet-stream";
	}

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
	public Map<Integer, FileInfo> getFilesByIds(List<Integer> fileIds) {
		if (fileIds == null || fileIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<FileInfo> fileInfos = fileRepository.selectFileInfosByIds(fileIds);

		return fileInfos.stream()
			.collect(Collectors.toMap(FileInfo::getFileId, fileInfo -> fileInfo));
	}

	@Override
	public List<FileInfo> getFiles() {
		return fileRepository.selectFileInfos();
	}

	@Override
	@Transactional
	public List<FileInfo> uploadBase64Batch(List<Base64ImageDto> list) {
		if (list == null || list.isEmpty())
			return List.of();
		List<FileInfo> out = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			Base64ImageDto dto = list.get(i);
			String name = (dto != null && dto.getFileName() != null) ? dto.getFileName() : "(no-name)";
			try {
				out.add(uploadBase64(dto)); // 내부에서 decodeBase64Safe 사용
			} catch (IllegalArgumentException e) {
				// 어떤 인덱스/이름에서 실패했는지 노출 → API 응답에 그대로 반영하면 디버깅 쉬움
				throw new IllegalArgumentException("이미지 #" + (i + 1) + " (" + name + ") 실패: " + e.getMessage(), e);
			}
		}
		return out;
	}

	@Override
	@Transactional
	public FileInfo uploadBase64(Base64ImageDto dto) {
		if (dto == null || dto.getDataUrl() == null || dto.getDataUrl().isBlank()) {
			throw new IllegalArgumentException("dataUrl이 비어 있습니다.");
		}
		byte[] bytes = decodeBase64Safe(dto.getDataUrl()); // ★ 강화된 디코더 사용
		String contentType = guessContentType(bytes, null);
		String safeName = (dto.getFileName() == null || dto.getFileName().isBlank())
			? ("image".concat(contentType != null && contentType.contains("png") ? ".png"
				: contentType != null && contentType.contains("jpeg") ? ".jpg" : ""))
			: dto.getFileName();

		try {
			String url = s3Service.uploadBytes(bytes, safeName, contentType);
			String key = url.substring(url.lastIndexOf('/') + 1);

			FileInfo fi = FileInfo.builder()
				.originalName(safeName)
				.s3Key(key)
				.s3Url(url)
				.bucketName(bucketName)
				.fileSize((long)bytes.length)
				.fileType(contentType)
				.uploadBy("ADMIN")
				.build();

			fileRepository.insertFileInfo(fi);
			return fi;
		} catch (IOException e) {
			throw new RuntimeException("S3 업로드 실패", e);
		}
	}

}
