package com.miracle.coordifit.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeUserDto {
	private String userId;
	private String nickname;
	private String profileImageUrl;
}
