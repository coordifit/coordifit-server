package com.miracle.coordifit.clothes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter @Setter
public class ClothesResponse {
    private String clothesId;
    private String name;
    private String categoryCode;
    private String categoryName; // 조인해서 내려줄 경우
    private String imageUrl;
}
