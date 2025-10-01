package com.miracle.coordifit.user.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miracle.coordifit.user.model.User;

@Mapper
public interface UserRepository {
	int insertUser(User user);

	User selectUserByEmail(@Param("email") String email);

	User selectUserByUserId(@Param("userId") String userId);

	int countByEmail(@Param("email") String email);

	int countByNickname(@Param("nickname") String nickname);

	int getNextUserSequence();

	int updateLastLoginTime(@Param("userId") String userId);
}
