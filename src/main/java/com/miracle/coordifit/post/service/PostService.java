package com.miracle.coordifit.post.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miracle.coordifit.post.dto.CommentResponseDto;
import com.miracle.coordifit.post.dto.PostClothesResponse;
import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.dto.PostDetailResponse;
import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.model.Post;
import com.miracle.coordifit.post.model.PostClothes;
import com.miracle.coordifit.post.model.PostImage;
import com.miracle.coordifit.post.repository.CommentRepository;
import com.miracle.coordifit.post.repository.LikeRepository;
import com.miracle.coordifit.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;

	@Override
	@Transactional
	public void createPost(PostCreateRequest request, String userId) {
		String postId = generatePostId();

		Post post = Post.builder()
			.postId(postId)
			.userId(userId)
			.content(request.getContent())
			.isPublic(request.getIsPublic() != null && request.getIsPublic() ? "Y" : "N")
			.isActive("Y")
			.createdBy(userId)
			.build();

		int result = postRepository.insertPost(post);
		if (result <= 0) {
			throw new RuntimeException("게시물 등록 처리 중 오류가 발생했습니다.");
		}

		if (request.getImageFileIds() != null && !request.getImageFileIds().isEmpty()) {
			for (Long fileId : request.getImageFileIds()) {
				PostImage postImage = PostImage.builder()
					.postId(postId)
					.fileId(fileId)
					.createdBy(userId)
					.build();

				result = postRepository.insertPostImage(postImage);
				if (result <= 0) {
					throw new RuntimeException("게시물 이미지 등록 처리 중 오류가 발생했습니다.");
				}
			}
		}

		if (request.getClothesIds() != null && !request.getClothesIds().isEmpty()) {
			for (String clothesId : request.getClothesIds()) {
				PostClothes postClothes = PostClothes.builder()
					.postId(postId)
					.clothesId(clothesId)
					.createdBy(userId)
					.build();

				result = postRepository.insertPostClothes(postClothes);
				if (result <= 0) {
					throw new RuntimeException("게시물 옷 정보 등록 처리 중 오류가 발생했습니다.");
				}
			}
		}
	}

	@Override
	@Transactional
	public PostDetailResponse getPostDetail(String postId, String userId) {
		PostDetailResponse postDetail = postRepository.getPostDetail(postId);
		if (postDetail == null) {
			throw new RuntimeException("게시물을 찾을 수 없습니다: " + postId);
		}

		postRepository.incrementViewCount(postId);

		List<String> imageUrls = postRepository.getPostImageUrls(postId);
		postDetail.setImageUrls(imageUrls);

		List<PostClothesResponse> clothes = postRepository.getPostClothes(postId);
		postDetail.setClothes(clothes);

		List<CommentResponseDto> comments = commentRepository.getCommentsByPostId(postId, userId);
		postDetail.setComments(comments);

		int isLiked = likeRepository.isLiked(userId, postId);
		postDetail.setLiked(isLiked > 0);

		return postDetail;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PostDto> getAllPosts() {
		return postRepository.getAllPosts();
	}

	private String generatePostId() {
		int nextSeq = postRepository.getNextPostSequence();
		return String.format("P%s%03d", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")), nextSeq);
	}
}
