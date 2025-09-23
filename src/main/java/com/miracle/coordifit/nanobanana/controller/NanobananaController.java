package com.miracle.coordifit.nanobanana.controller;

import com.miracle.coordifit.nanobanana.dto.FittingRequestDTO;
import com.miracle.coordifit.nanobanana.dto.PromptRequestDTO;
import com.miracle.coordifit.nanobanana.service.NanobananaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

/**
 * 이미지 생성 및 가상 피팅 요청을 처리하는 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/nanobanana")
public class NanobananaController {

    private final NanobananaService nanobananaService;

    public NanobananaController(NanobananaService nanobananaService) {
        this.nanobananaService = nanobananaService;
    }

    /**
     * 단순 프롬프트 기반 이미지 생성
     */
    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generateImage(@RequestBody PromptRequestDTO request) {
        return nanobananaService.generateImage(request.getPrompt())
                .map(base64Image -> ResponseEntity.ok().body(base64Image))
                .onErrorResume(e -> {
                    System.err.println("Error during image generation: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body("Image generation failed."));
                });
    }

    /**
     * 아바타 + 의류 기반 가상 피팅 이미지 생성 (JSON 버전)
     */
    @PostMapping("/fitting")
    public Mono<ResponseEntity<Map<String, Object>>> fitting(@RequestBody FittingRequestDTO request) {
        String prompt = buildPrompt(request);

        long start = System.currentTimeMillis();
        return nanobananaService.generateImage(prompt)
                .map(base64 -> {
                    long duration = System.currentTimeMillis() - start;
                    Map<String, Object> response = Map.of(
                            "status", "success",
                            "data", Map.of(
                                    "imageBase64", base64,
                                    "durationMs", duration
                            )
                    );
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    System.err.println("Error during fitting: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                            "status", "error",
                            "message", "Fitting failed"
                    )));
                });
    }

    /**
     * 아바타 + 의류 기반 가상 피팅 이미지 생성 (파일 업로드 버전)
     */
    @PostMapping(value = "/fitting/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> fittingUpload(
            @RequestPart("avatar") MultipartFile avatar,
            @RequestPart(value = "top", required = false) MultipartFile top,
            @RequestPart(value = "bottom", required = false) MultipartFile bottom,
            @RequestPart(value = "shoes", required = false) MultipartFile shoes
    ) {
        try {
            String avatarBase64 = Base64.getEncoder().encodeToString(avatar.getBytes());
            String topBase64 = top != null ? Base64.getEncoder().encodeToString(top.getBytes()) : null;
            String bottomBase64 = bottom != null ? Base64.getEncoder().encodeToString(bottom.getBytes()) : null;
            String shoesBase64 = shoes != null ? Base64.getEncoder().encodeToString(shoes.getBytes()) : null;

            String prompt = buildPromptWithFiles(avatarBase64, topBase64, bottomBase64, shoesBase64);

            long start = System.currentTimeMillis();
            return nanobananaService.generateImage(prompt)
                    .map(base64 -> {
                        long duration = System.currentTimeMillis() - start;
                        Map<String, Object> response = Map.of(
                                "status", "success",
                                "data", Map.of(
                                        "imageBase64", base64,
                                        "durationMs", duration
                                )
                        );
                        return ResponseEntity.ok(response);
                    })
                    .onErrorResume(e -> {
                        System.err.println("Error during fitting upload: " + e.getMessage());
                        return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                                "status", "error",
                                "message", "Fitting upload failed"
                        )));
                    });

        } catch (Exception e) {
            return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "File processing failed: " + e.getMessage()
            )));
        }
    }

    /**
     * 프롬프트 빌더 (멀티 버전 지원: URL/Base64 or ID 기반)
     */
    private String buildPrompt(FittingRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("아바타 이미지를 기반으로 선택된 의류를 입힌 가상 피팅 이미지를 생성해주세요.\n");

        if (request.getAvatarImage() != null) {
            sb.append("아바타 이미지: ").append(request.getAvatarImage()).append("\n");
        } else if (request.getAvatarId() != null) {
            sb.append("아바타 ID: ").append(request.getAvatarId()).append("\n");
        }

        if (request.getClothesImages() != null && !request.getClothesImages().isEmpty()) {
            sb.append("의류 이미지들:\n");
            for (int i = 0; i < request.getClothesImages().size(); i++) {
                sb.append("- 의류 ").append(i + 1).append(": ").append(request.getClothesImages().get(i)).append("\n");
            }
        } else if (request.getClothesIds() != null && !request.getClothesIds().isEmpty()) {
            sb.append("의류 ID들: ").append(String.join(", ", request.getClothesIds())).append("\n");
        } else {
            sb.append("선택된 의류 없음\n");
        }

        sb.append("상의는 상체, 하의는 하체, 신발은 발 위치에 자연스럽게 배치해서 합성해주세요.");
        return sb.toString();
    }

    /**
     * 파일 업로드 버전 프롬프트 빌더
     */
    private String buildPromptWithFiles(String avatar, String top, String bottom, String shoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("아바타 이미지를 기반으로 선택된 의류를 입힌 가상 피팅 이미지를 생성해주세요.\n");
        sb.append("아바타: [Base64 인코딩된 이미지 포함]\n");

        if (top != null) sb.append("상의 이미지를 합성해주세요.\n");
        if (bottom != null) sb.append("하의 이미지를 합성해주세요.\n");
        if (shoes != null) sb.append("신발 이미지를 합성해주세요.\n");

        sb.append("상의는 상체, 하의는 하체, 신발은 발 위치에 자연스럽게 배치해서 합성해주세요.");
        return sb.toString();
    }
}
