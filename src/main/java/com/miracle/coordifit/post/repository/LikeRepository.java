package com.miracle.coordifit.post.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.post.dto.LikeUserDto;
import com.miracle.coordifit.post.model.Like;

@Mapper
public interface LikeRepository {

	int isLiked(@Param("userId") String userId, @Param("targetId") String targetId);

	int insertLike(Like like);

	int deleteLike(@Param("userId") String userId, @Param("targetId") String targetId);

	int updatePostLikeCount(@Param("postId") String postId);

	int updateCommentLikeCount(@Param("commentId") String commentId);

	List<LikeUserDto> getLikeUsersByTargetId(@Param("targetId") String targetId);
}
