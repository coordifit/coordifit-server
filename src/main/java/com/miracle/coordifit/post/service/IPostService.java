package com.miracle.coordifit.post.service;

import java.util.List;

import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.dto.PostDetailResponse;
import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.model.Post;

public interface IPostService {

	Post createPost(PostCreateRequest request, String userId);

	PostDetailResponse getPostDetail(String postId, String userId);

	void incrementViewCount(String postId);

	List<PostDto> getAllPosts();
}
