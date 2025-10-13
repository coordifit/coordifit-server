package com.miracle.coordifit.user.service;

import com.miracle.coordifit.user.dto.MyPageResponseDto;

public interface IMyPageService {

	MyPageResponseDto getMyPageInfo(String userId, String currentUserId);
}
