package com.miracle.coordifit.clothes.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClothesDetailResponseDto {
    private String clothesId;
    private String userId;
    private String name;
    private String brand;
    private String categoryCode;
    private String categoryName;
    private String clothesSize;
    private BigDecimal price;
    private LocalDate purchaseDate;
    private String purchaseUrl;
    private String description;
    private Integer wearCount;
    private LocalDate lastWornDate;
    private String isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> imageUrls; // S3 URL들
}
