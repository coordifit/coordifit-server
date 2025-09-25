package com.miracle.coordifit.common.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {
    private Integer fileId;
    private String originalName;
    private String s3Key;
    private String s3Url;
    private String bucketName;
    private Long fileSize;
    private String fileType;
    private String uploadBy;
    private LocalDateTime uploadDate;
}
