package com.miracle.coordifit.post.service;

import java.util.List;

import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.dto.PostDetailResponse;
import com.miracle.coordifit.post.dto.PostDto;

public interface IPostService {

	void createPost(PostCreateRequest request, String userId);

	PostDetailResponse getPostDetail(String postId, String userId);

	List<PostDto> getAllPosts();
}
