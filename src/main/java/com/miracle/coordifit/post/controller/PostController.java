package com.miracle.coordifit.post.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.post.dto.CommentResponseDto;
import com.miracle.coordifit.post.dto.LikeUserDto;
import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.dto.PostDetailResponse;
import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.service.ICommentService;
import com.miracle.coordifit.post.service.ILikeService;
import com.miracle.coordifit.post.service.IPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final IPostService postService;
	private final ICommentService commentService;
	private final ILikeService likeService;

	@GetMapping
	public ResponseEntity<ApiResponseDto<List<PostDto>>> getAllPosts() {
		try {
			List<PostDto> posts = postService.getAllPosts();
			log.info("전체 게시물 조회 완료: {} 개", posts.size());
			return ResponseEntity.ok(ApiResponseDto.success("전체 게시물 조회 성공", posts));
		} catch (Exception e) {
			log.error("전체 게시물 조회 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("전체 게시물 조회 실패: " + e.getMessage()));
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponseDto<Void>> createPost(
		@RequestBody PostCreateRequest request,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			postService.createPost(request, userId);

			log.info("게시물 등록 완료: userId={}", userId);
			return ResponseEntity.ok(ApiResponseDto.success("게시물 등록 완료"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("게시물 등록 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponseDto<PostDetailResponse>> getPostDetail(
		@PathVariable String postId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			PostDetailResponse postDetail = postService.getPostDetail(postId, userId);

			log.info("게시물 상세 조회 완료: postId={}", postId);
			return ResponseEntity.ok(ApiResponseDto.success("게시물 상세 조회 성공", postDetail));
		} catch (Exception e) {
			log.error("게시물 상세 조회 실패: postId={}", postId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("게시물 상세 조회 실패: " + e.getMessage()));
		}
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<ApiResponseDto<Void>> togglePostLike(
		@PathVariable String postId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			likeService.toggleLike(postId, "POST", userId);

			log.info("게시글 좋아요 토글 완료: postId={}", postId);
			return ResponseEntity.ok(ApiResponseDto.success("좋아요 처리 완료"));
		} catch (Exception e) {
			log.error("게시글 좋아요 처리 실패: postId={}", postId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("좋아요 처리 실패: " + e.getMessage()));
		}
	}

	@PostMapping("/{postId}/comments")
	public ResponseEntity<ApiResponseDto<Void>> createComment(
		@PathVariable String postId,
		@RequestParam String content,
		@RequestParam(required = false) String parentId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			commentService.createComment(postId, content, parentId, userId);

			log.info("댓글 등록 완료: postId={}", postId);
			return ResponseEntity.ok(ApiResponseDto.success("댓글 등록 완료"));
		} catch (Exception e) {
			log.error("댓글 등록 실패: postId={}", postId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("댓글 등록 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{postId}/comments")
	public ResponseEntity<ApiResponseDto<List<CommentResponseDto>>> getComments(
		@PathVariable String postId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId, userId);

			log.info("댓글 목록 조회 완료: postId={}, count={}", postId, comments.size());
			return ResponseEntity.ok(ApiResponseDto.success("댓글 목록 조회 성공", comments));
		} catch (Exception e) {
			log.error("댓글 목록 조회 실패: postId={}", postId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("댓글 목록 조회 실패: " + e.getMessage()));
		}
	}

	@PostMapping("/comments/{commentId}/like")
	public ResponseEntity<ApiResponseDto<Void>> toggleCommentLike(
		@PathVariable String commentId,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			likeService.toggleLike(commentId, "COMMENT", userId);

			log.info("댓글 좋아요 토글 완료: commentId={}", commentId);
			return ResponseEntity.ok(ApiResponseDto.success("댓글 좋아요 처리 완료"));
		} catch (Exception e) {
			log.error("댓글 좋아요 처리 실패: commentId={}", commentId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("댓글 좋아요 처리 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/{postId}/likes")
	public ResponseEntity<ApiResponseDto<List<LikeUserDto>>> getPostLikes(
		@PathVariable String postId) {
		try {
			List<LikeUserDto> likeUsers = likeService.getLikeUsers(postId);

			log.info("게시글 좋아요 목록 조회 완료: postId={}, count={}", postId, likeUsers.size());
			return ResponseEntity.ok(ApiResponseDto.success("좋아요 목록 조회 성공", likeUsers));
		} catch (Exception e) {
			log.error("게시글 좋아요 목록 조회 실패: postId={}", postId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("좋아요 목록 조회 실패: " + e.getMessage()));
		}
	}
}
