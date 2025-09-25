package com.miracle.coordifit.common.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service implements IS3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
	private String bucket;

    @Value("${aws.s3.region}")
	private String region;
    
    @Override
	public String uploadFile(MultipartFile file) throws IOException {
		String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
		
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
	}    
}


