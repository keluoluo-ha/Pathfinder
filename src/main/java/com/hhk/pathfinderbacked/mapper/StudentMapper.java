package com.hhk.pathfinderbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhk.pathfinderbacked.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
