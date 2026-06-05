package com.hhk.pathfinderbacked.forum.support;

import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.forum.vo.ForumAuthorVO;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForumUserSupport {

    private final StudentMapper studentMapper;

    public Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    public Student requireStudent(Long userId) {
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return student;
    }

    public ForumAuthorVO toAuthor(Student student) {
        if (student == null) {
            return null;
        }
        ForumAuthorVO vo = new ForumAuthorVO();
        vo.setUserId(student.getId());
        vo.setNickname(student.getNickname() != null && !student.getNickname().isBlank()
                ? student.getNickname() : student.getName());
        vo.setAvatarUrl(student.getAvatarUrl());
        vo.setSubjectType(student.getSubjectType());
        return vo;
    }

    public ForumAuthorVO loadAuthor(Long userId) {
        if (userId == null) {
            return null;
        }
        return toAuthor(studentMapper.selectById(userId));
    }
}
