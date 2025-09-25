package com.miracle.coordifit.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miracle.coordifit.clothes.dto.CodeDto;
import com.miracle.coordifit.clothes.dto.ClothesCreateRequestDto;
import com.miracle.coordifit.clothes.dto.ClothesDetailResponseDto;
import com.miracle.coordifit.clothes.service.ClothesService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/clothes")
public class ClothesController {

    private final ClothesService clothesService;
    private final ObjectMapper objectMapper;

    public ClothesController(ClothesService clothesService, ObjectMapper objectMapper) {
        this.clothesService = clothesService;
        this.objectMapper = objectMapper;
    }

    // 이미지 필수 등록 (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothesDetailResponseDto> create(
            @RequestParam("data") String dataJson,
            @RequestPart("files") List<MultipartFile> files,
            Principal principal
    ) throws IOException {
        @Valid ClothesCreateRequestDto req = objectMapper.readValue(dataJson, ClothesCreateRequestDto.class);
        String userId = resolveUserId(principal, null);
        return ResponseEntity.ok(clothesService.createWithImages(req, userId, files));
    }

    // 조회
    @GetMapping("/{clothesId}")
    public ResponseEntity<ClothesDetailResponseDto> detail(@PathVariable String clothesId) {
        return ResponseEntity.ok(clothesService.getDetail(clothesId));
    }

    @GetMapping
    public ResponseEntity<List<ClothesDetailResponseDto>> list(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String categoryCode,
            Principal principal) {
        String resolvedUserId = resolveUserId(principal, userId);
        return ResponseEntity.ok(clothesService.getList(resolvedUserId, categoryCode));
    }

    // 카테고리(공통코드) - 상위/하위/트리
    @GetMapping("/categories/parents")
    public ResponseEntity<List<CodeDto>> parents() {
        return ResponseEntity.ok(clothesService.getTopCategories());
    }

    @GetMapping("/categories/children")
    public ResponseEntity<List<CodeDto>> children(@RequestParam String parentCodeId) {
        return ResponseEntity.ok(clothesService.getChildren(parentCodeId));
    }

    @GetMapping("/categories/tree")
    public ResponseEntity<List<CodeDto>> tree() {
        return ResponseEntity.ok(clothesService.getTree());
    }

    private String resolveUserId(Principal principal, String explicitUserId) {
        if (explicitUserId != null && !explicitUserId.isBlank()) return explicitUserId;
        if (principal != null && principal.getName() != null
                && !principal.getName().isBlank()
                && !"anonymousUser".equalsIgnoreCase(principal.getName())) {
            return principal.getName();
        }
        return "U000001"; // 로컬 기본 유저
    }
}
