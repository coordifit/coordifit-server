package com.miracle.coordifit.post.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {
	private String content;
	private Boolean isPublic;
	private List<Long> imageFileIds;
	private List<String> clothesIds;
}
