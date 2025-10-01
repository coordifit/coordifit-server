package com.miracle.coordifit.user.service;

import com.miracle.coordifit.auth.dto.SignUpRequestDto;
import com.miracle.coordifit.user.dto.ProfileUpdateRequestDto;
import com.miracle.coordifit.user.model.User;

public interface IUserService {
	User signUp(SignUpRequestDto signUpRequestDto);

	boolean isEmailAvailable(String email);

	boolean isNicknameAvailable(String nickname);

	boolean updateLastLoginTime(String userId);

	User authenticate(String email, String password);

	User getUserById(String userId);

	void updateUserProfile(String userId, ProfileUpdateRequestDto requestDto);
}
