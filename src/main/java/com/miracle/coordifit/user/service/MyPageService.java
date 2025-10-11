package com.miracle.coordifit.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miracle.coordifit.post.dto.PostDto;
import com.miracle.coordifit.user.dto.MyPageResponseDto;
import com.miracle.coordifit.user.repository.MyPageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService implements IMyPageService {

	private final MyPageRepository myPageRepository;

	@Override
	public MyPageResponseDto getMyPageInfo(String userId, String currentUserId) {
		MyPageResponseDto myPageInfo = myPageRepository.getMyPageInfo(userId);
		if (myPageInfo == null) {
			throw new RuntimeException("사용자 정보를 찾을 수 없습니다: " + userId);
		}

		List<PostDto> posts = myPageRepository.getUserPosts(userId);
		myPageInfo.setPostsCount(posts.size());
		myPageInfo.setPosts(posts);

		return myPageInfo;
	}
}
