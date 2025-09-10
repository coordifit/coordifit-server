package com.miracle.coordifit.s3;

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
public class S3Service {
    private final S3Client s3;
    @Value("${aws.s3.bucket}")
	private String bucket;
    @Value("${aws.s3.region}")
	private String region;
    
	public String uploadFile(MultipartFile file) throws IOException {
		String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
		
        s3.putObject(
    		PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromBytes(file.getBytes())
        );

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
	}

};
