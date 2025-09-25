package com.miracle.coordifit.clothes.dto;

import java.util.List;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeDto {
    private String codeId;         // ex) B20001, B30001
    private String codeName;       // ex) 상의, 셔츠
    private String parentCodeId;   // ex) B10001, B20001
    private List<CodeDto> children;
}
