package com.miracle.coordifit.clothes.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClothesBulkCreateRequest {

    @NotEmpty
    private List<ClothesCreateRequest> items;
}
