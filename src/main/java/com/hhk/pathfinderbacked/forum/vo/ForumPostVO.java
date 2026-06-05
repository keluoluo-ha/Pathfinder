package com.hhk.pathfinderbacked.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPostVO {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long userId;
    private ForumAuthorVO author;
    private String title;
    private String content;
    private String category;
    private String grade;
    private Integer gaokaoType;
    private String subjects;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createTime;
}
