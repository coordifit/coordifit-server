package com.miracle.coordifit.user.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequestDto {
	private String nickname;
	private Long fileId;
	private String genderCode;
	private String birthDate;
	private String isActive;
}
