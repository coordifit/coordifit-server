package com.miracle.coordifit.nanobanana.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Google Gemini API에 이미지를 요청할 때 사용되는 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequestDTO {
    private List<Contents> contents;
    private GenerationConfig generationConfig;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contents {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        private List<String> responseModalities;
    }
}
