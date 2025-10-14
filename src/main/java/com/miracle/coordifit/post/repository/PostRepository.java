package com.miracle.coordifit.post.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.model.Post;
import com.miracle.coordifit.post.model.PostClothes;
import com.miracle.coordifit.post.model.PostImage;

@Mapper
public interface PostRepository {

	int getNextPostSequence();

	int insertPost(Post post);

	int insertPostImage(PostImage postImage);

	int insertPostClothes(PostClothes postClothes);

	List<PostDto> getAllPosts();
}
