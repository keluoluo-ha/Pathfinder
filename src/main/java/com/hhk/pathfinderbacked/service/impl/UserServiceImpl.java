package com.hhk.pathfinderbacked.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhk.pathfinderbacked.cache.CacheConfig;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.dto.UserLoginRequest;
import com.hhk.pathfinderbacked.dto.UserRegisterRequest;
import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.service.UserService;
import com.hhk.pathfinderbacked.utils.JwtUtil;
import com.hhk.pathfinderbacked.utils.UserContext;
import com.hhk.pathfinderbacked.vo.LoginVO;
import com.hhk.pathfinderbacked.vo.UserOverviewVO;
import com.hhk.pathfinderbacked.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final StudentMapper studentMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(UserRegisterRequest request) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getMobile, request.getMobile());
        Student exist = studentMapper.selectOne(queryWrapper);
        if (exist != null) {
            throw new BusinessException(ErrorCode.MOBILE_EXISTS);
        }
        Student student = new Student();
        student.setStudentNo("S" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        student.setName(request.getName());
        student.setMobile(request.getMobile());
        student.setPassword(SecureUtil.sha256(request.getPassword()));
        student.setSubjectType(request.getSubjectType());
        student.setScore(request.getScore());
        student.setRankNo(request.getRankNo());
        student.setProvince("广东");
        studentMapper.insert(student);
    }

    @Override
    public LoginVO login(UserLoginRequest request) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getMobile, request.getMobile()));
        if (student == null || !SecureUtil.sha256(request.getPassword()).equals(student.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        String token = jwtUtil.createToken(student.getId(), student.getSubjectType());
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpireAt(jwtUtil.getExpireAt(token));
        loginVO.setProfile(BeanUtil.copyProperties(student, UserProfileVO.class));
        return loginVO;
    }

    @Override
    public void logout(String token) {
        jwtUtil.verifyToken(token);
        long expireAt = jwtUtil.getExpireAt(token);
        long ttlMillis = Math.max(1, expireAt - System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(
                CacheConfig.JWT_BLACKLIST_PREFIX + token,
                "1",
                Duration.ofMillis(ttlMillis)
        );
    }

    @Override
    public UserProfileVO getCurrentProfile() {
        Student student = getCurrentStudent();
        return BeanUtil.copyProperties(student, UserProfileVO.class);
    }

    @Override
    public UserOverviewVO getCurrentOverview() {
        Student student = getCurrentStudent();
        UserOverviewVO overview = new UserOverviewVO();

        UserOverviewVO.Profile profile = new UserOverviewVO.Profile();
        profile.setAvatarUrl(student.getAvatarUrl());
        profile.setNickname(student.getNickname());
        profile.setSignature(student.getSignature());
        overview.setProfile(profile);

        overview.setStarCoin(Objects.requireNonNullElse(student.getStarCoin(), 0));
        overview.setBalance(Objects.requireNonNullElse(student.getBalance(), BigDecimal.ZERO));

        UserOverviewVO.Message message = new UserOverviewVO.Message();
        message.setUnreadCount(Objects.requireNonNullElse(student.getUnreadMessageCount(), 0));
        overview.setMessage(message);

        UserOverviewVO.Exchange exchange = new UserOverviewVO.Exchange();
        exchange.setRecordCount(Objects.requireNonNullElse(student.getExchangeRecordCount(), 0));
        overview.setExchange(exchange);

        UserOverviewVO.Volunteer volunteer = new UserOverviewVO.Volunteer();
        volunteer.setHours(Objects.requireNonNullElse(student.getVolunteerHours(), BigDecimal.ZERO));
        overview.setVolunteer(volunteer);

        UserOverviewVO.Planning planning = new UserOverviewVO.Planning();
        planning.setPhone(student.getPlanningContactPhone());
        overview.setPlanning(planning);
        return overview;
    }

    private Student getCurrentStudent() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return student;
    }
}
