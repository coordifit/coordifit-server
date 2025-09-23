package com.miracle.coordifit.nanobanana.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 가상 피팅 요청 DTO
 * - 현재: avatarImage + clothesImages (URL/Base64)
 * - 추후: avatarId + clothesIds (DB 기반)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FittingRequestDTO {
    // 테스트용 (직접 이미지 전달)
    private String avatarImage;          // 아바타 이미지 (URL/Base64)
    private List<String> clothesImages;  // 의류 이미지들 (URL/Base64)

    // 향후 DB 연동용
    private String avatarId;             // 아바타 ID
    private List<String> clothesIds;     // 의류 ID 목록
}
