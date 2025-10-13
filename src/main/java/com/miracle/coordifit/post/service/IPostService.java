package com.miracle.coordifit.post.service;

import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.model.Post;

public interface IPostService {

	Post createPost(PostCreateRequest request, String userId);
}
