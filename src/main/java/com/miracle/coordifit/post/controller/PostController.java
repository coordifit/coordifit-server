package com.miracle.coordifit.post.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miracle.coordifit.common.dto.ApiResponseDto;
import com.miracle.coordifit.post.dto.PostCreateRequest;
import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.post.model.Post;
import com.miracle.coordifit.post.service.IPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final IPostService postService;

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
	public ResponseEntity<ApiResponseDto<Post>> createPost(
		@RequestBody PostCreateRequest request,
		Authentication authentication) {
		try {
			String userId = authentication.getName();
			Post post = postService.createPost(request, userId);

			log.info("게시물 등록 완료: postId={}", post.getPostId());
			return ResponseEntity.ok(ApiResponseDto.success("게시물 등록 완료", post));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseDto.error("게시물 등록 실패: " + e.getMessage()));
		}
	}
}
