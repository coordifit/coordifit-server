package com.miracle.coordifit.clothes.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter; 
import lombok.Setter;

@Getter @Setter
public class ClothesUpdateRequest {
    @NotBlank private String clothesId;
    private String name;
    private String categoryCode;
    private String brand;
    private String clothesSize;
    private Integer price;
    private LocalDate purchaseDate;
    private String purchaseUrl;
    private String description;

    private List<Long> fileIds;


}
