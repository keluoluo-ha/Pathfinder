package com.hhk.pathfinderbacked.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_material")
public class ForumMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String fileUrl;
    private String subject;
    private String grade;
    private Long userId;
    private Integer downloadCount;
    private Integer status;
    private LocalDateTime createTime;
}
