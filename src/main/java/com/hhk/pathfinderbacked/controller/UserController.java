package com.hhk.pathfinderbacked.controller;

import com.hhk.pathfinderbacked.common.Result;
import com.hhk.pathfinderbacked.dto.UserLoginRequest;
import com.hhk.pathfinderbacked.dto.UserRegisterRequest;
import com.hhk.pathfinderbacked.service.UserService;
import com.hhk.pathfinderbacked.vo.LoginVO;
import com.hhk.pathfinderbacked.vo.UserOverviewVO;
import com.hhk.pathfinderbacked.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(security = {})
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(security = {})
    public Result<LoginVO> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        userService.logout(token);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.success(userService.getCurrentProfile());
    }

    @GetMapping("/overview")
    public Result<UserOverviewVO> overview() {
        return Result.success(userService.getCurrentOverview());
    }
}
