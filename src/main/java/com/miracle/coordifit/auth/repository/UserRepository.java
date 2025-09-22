package com.miracle.coordifit.auth.repository;

import com.miracle.coordifit.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {    
    int insertUser(User user);
    User selectUserByEmail(@Param("email") String email);    
    int countByEmail(@Param("email") String email);
    int countByNickname(@Param("nickname") String nickname);
    int getNextUserSequence();
    int updateLastLoginTime(@Param("userId") String userId);
}
