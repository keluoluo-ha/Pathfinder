package com.hhk.pathfinderbacked.service;

import com.hhk.pathfinderbacked.dto.UserLoginRequest;
import com.hhk.pathfinderbacked.dto.UserRegisterRequest;
import com.hhk.pathfinderbacked.vo.LoginVO;
import com.hhk.pathfinderbacked.vo.UserOverviewVO;
import com.hhk.pathfinderbacked.vo.UserProfileVO;

public interface UserService {

    void register(UserRegisterRequest request);

    LoginVO login(UserLoginRequest request);

    void logout(String token);

    UserProfileVO getCurrentProfile();

    UserOverviewVO getCurrentOverview();
}
