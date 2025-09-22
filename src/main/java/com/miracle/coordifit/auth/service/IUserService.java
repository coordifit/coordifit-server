package com.miracle.coordifit.auth.service;

import com.miracle.coordifit.auth.model.User;
import com.miracle.coordifit.auth.dto.SignUpRequestDto;

public interface IUserService {        
    User signUp(SignUpRequestDto signUpRequestDto);
    boolean isEmailAvailable(String email);
    boolean isNicknameAvailable(String nickname);
    boolean updateLastLoginTime(String userId);
    User authenticate(String email, String password);
}
