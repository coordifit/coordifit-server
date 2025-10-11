package com.miracle.coordifit.user.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.user.dto.MyPageResponseDto;

@Mapper
public interface MyPageRepository {

	MyPageResponseDto getMyPageInfo(@Param("userId") String userId);

	List<PostDto> getUserPosts(@Param("userId") String userId);
}
