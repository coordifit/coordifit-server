package com.miracle.coordifit.auth.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // 비밀번호는 로그에서 제외
@Builder
public class User {
	// @formatter:off
	private String userId;			// U + 6자리 (U000001, U000002...)
	private String email;			// 이메일 로그인용, 소셜 로그인 시 NULL 허용
	private String password;		// BCrypt 암호화된 비밀번호, 소셜 로그인 시 NULL
	private String nickname;		// 사용자 표시명, 중복 방지
	private Long fileId;			// 프로필 이미지 파일 ID (FILE_INFO 참조)
	private String loginTypeCode;	// 로그인 방식 공통코드
	private String kakaoId;			// 카카오 고유 ID, 카카오 로그인 시만 사용
	private String genderCode;		// 성별 공통코드
	private LocalDateTime birthDate;// 생년월일, 선택사항
	private String isActive;		// 계정 활성화 상태
	private LocalDateTime createdAt;// 가입일
	private String createdBy;		// 생성자 ID (시스템 또는 관리자)
	private LocalDateTime updatedAt;// 마지막 수정일
	private String updatedBy;		// 수정자 ID
	private LocalDateTime loginedAt;// 마지막 로그인 시간
	// @formatter:on
}
