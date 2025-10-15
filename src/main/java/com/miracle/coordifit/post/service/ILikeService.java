package com.miracle.coordifit.post.service;

import java.util.List;

import com.miracle.coordifit.post.dto.LikeUserDto;

public interface ILikeService {

	void toggleLike(String targetId, String targetType, String userId);

	List<LikeUserDto> getLikeUsers(String targetId);
}
