// src/main/java/com/miracle/coordifit/clothes/controller/ClothesController.java
package com.miracle.coordifit.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miracle.coordifit.clothes.model.Clothes;
import com.miracle.coordifit.clothes.service.IClothesService;
import com.miracle.coordifit.common.model.CommonCode;
import com.miracle.coordifit.common.model.FileInfo;
import com.miracle.coordifit.common.service.ICommonCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Clothes", description = "옷 등록/수정/조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

    private final IClothesService clothesService;
    private final ICommonCodeService commonCodeService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "상위 카테고리 목록")
    @GetMapping("/categories/roots")
    public ResponseEntity<?> categoryRoots() {
        Map<String, CommonCode> roots = commonCodeService.getCommonCodes();
        CommonCode designatedRoot = roots.get("B10001");
        if (designatedRoot != null) {
            return ResponseEntity.ok(new ArrayList<>(designatedRoot.getChildren().values()));
        }
        List<CommonCode> rootList = roots.values().stream()
                .filter(cc -> cc.getLevel() == 1 || cc.getParentCodeId() == null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rootList);
    }

    @Operation(summary = "하위 카테고리 목록")
    @GetMapping("/categories/{parentCodeId}/children")
    public ResponseEntity<?> categoryChildren(@PathVariable String parentCodeId) {
        Map<String, CommonCode> roots = commonCodeService.getCommonCodes();
        CommonCode parent = findCodeById(roots, parentCodeId);
        if (parent == null) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(new ArrayList<>(parent.getChildren().values()));
    }

    @Operation(
            summary = "옷 등록 (이미지 1~5장 필수)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @Parameter(
                    name = "clothes",
                    description = "옷 정보(JSON 문자열). 예: {\"name\":\"셔츠\",\"categoryCode\":\"B30001\"}",
                    required = true,
                    content = @Content(schema = @Schema(type = "string"))
            )
            @RequestPart("clothes") String clothesJson,
            @Parameter(
                    name = "images",
                    description = "이미지 파일(1~5장)",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            )
            @RequestPart("images") List<MultipartFile> images,
            @Parameter(description = "로그인한 사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId
    ) {
        try {
            if (images == null || images.size() < 1 || images.size() > 5) {
                return ResponseEntity.badRequest().body(error("이미지는 1~5장 업로드해야 합니다."));
            }
            Clothes clothes = objectMapper.readValue(clothesJson, Clothes.class);
            if (isBlank(clothes.getName())) return ResponseEntity.badRequest().body(error("name은 필수입니다."));
            if (isBlank(clothes.getCategoryCode())) return ResponseEntity.badRequest().body(error("categoryCode는 필수입니다."));
            String id = clothesService.register(clothes, images, userId);
            return ResponseEntity.ok(success(Map.of("clothesId", id)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error(root(e)));
        }
    }

    @Operation(
            summary = "옷 수정 (같은 폼 재사용)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @PutMapping(path = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modify(
            @Parameter(description = "옷 ID", required = true) @PathVariable String clothesId,
            @Parameter(
                    name = "clothes",
                    description = "수정할 옷 정보(JSON 문자열)",
                    required = true,
                    content = @Content(schema = @Schema(type = "string"))
            )
            @RequestPart("clothes") String clothesJson,
            @Parameter(
                    name = "addImages",
                    description = "추가 이미지 파일(0~5장, 총합 5장 이하 유지)",
                    required = false,
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            )
            @RequestPart(value = "addImages", required = false) List<MultipartFile> addImages,
            @Parameter(description = "true면 이미지 전체 교체", required = false)
            @RequestParam(defaultValue = "false") boolean replaceAllImages,
            @Parameter(description = "로그인한 사용자 ID", required = true)
            @RequestHeader("X-User-Id") String userId
    ) {
        try {
            Clothes clothes = objectMapper.readValue(clothesJson, Clothes.class);
            clothes.setClothesId(clothesId);
            clothes.setUserId(userId);
            clothesService.modify(clothes, addImages, replaceAllImages, userId);
            return ResponseEntity.ok(success());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error(root(e)));
        }
    }

    @Operation(summary = "이미지 개별 삭제")
    @DeleteMapping("/{clothesId}/images/{fileId}")
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "옷 ID", required = true) @PathVariable String clothesId,
            @Parameter(description = "파일 ID", required = true) @PathVariable Long fileId
    ) {
        try {
            clothesService.removeImage(clothesId, fileId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error(root(e)));
        }
    }

    @Operation(summary = "옷 삭제")
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<?> remove(@PathVariable String clothesId) {
        try {
            clothesService.remove(clothesId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error(root(e)));
        }
    }

    @Operation(summary = "옷 상세")
    @GetMapping("/{clothesId}")
    public ResponseEntity<Clothes> detail(@PathVariable String clothesId) {
        return ResponseEntity.ok(clothesService.findOne(clothesId));
    }

    @Operation(summary = "옷 이미지 목록")
    @GetMapping("/{clothesId}/images")
    public ResponseEntity<List<FileInfo>> images(@PathVariable String clothesId) {
        return ResponseEntity.ok(clothesService.findImages(clothesId));
    }

    @Operation(summary = "내 옷 전체 조회")
    @GetMapping("/me")
    public ResponseEntity<?> myClothes(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(clothesService.findMine(userId));
    }

    private CommonCode findCodeById(Map<String, CommonCode> map, String targetId) {
        if (map.containsKey(targetId)) return map.get(targetId);
        for (CommonCode c : map.values()) {
            CommonCode found = findCodeById(c.getChildren(), targetId);
            if (found != null) return found;
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Map<String, Object> success() {
        return Map.of("success", true);
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> error(String message) {
        return Map.of("success", false, "message", message);
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getClass().getSimpleName() + ": " + String.valueOf(r.getMessage());
    }
}
