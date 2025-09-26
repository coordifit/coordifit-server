package com.miracle.coordifit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsProperties {
	private String bucket;
	private String region;
}
