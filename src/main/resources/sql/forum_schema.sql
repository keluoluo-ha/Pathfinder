-- PathFinder 学生论坛表结构（无外键，user_id 逻辑关联 student.id）
USE pathfinder;

CREATE TABLE IF NOT EXISTS forum_board (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL COMMENT '板块名称',
    description VARCHAR(255) NULL COMMENT '板块描述',
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛板块';

CREATE TABLE IF NOT EXISTS forum_post (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_id      BIGINT       NOT NULL COMMENT '板块ID',
    user_id       BIGINT       NOT NULL COMMENT '发帖学生ID',
    title         VARCHAR(200) NOT NULL,
    content       TEXT         NOT NULL,
    category      VARCHAR(32)  NULL COMMENT '分类标签',
    grade         VARCHAR(16)  NULL COMMENT '年级',
    gaokao_type   TINYINT      NULL COMMENT '1物理 2历史',
    subjects      VARCHAR(32)  NULL COMMENT '选科组合',
    view_count    INT          NOT NULL DEFAULT 0,
    like_count    INT          NOT NULL DEFAULT 0,
    comment_count INT          NOT NULL DEFAULT 0,
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_board_time (board_id, create_time),
    INDEX idx_user (user_id),
    INDEX idx_filter (category, grade, gaokao_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

CREATE TABLE IF NOT EXISTS forum_comment (
    id          BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    parent_id   BIGINT   NOT NULL DEFAULT 0,
    content     TEXT     NOT NULL,
    status      TINYINT  NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_time (post_id, create_time),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论';

CREATE TABLE IF NOT EXISTS forum_message (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    from_user_id  BIGINT       NOT NULL,
    to_user_id    BIGINT       NOT NULL,
    content       VARCHAR(500) NOT NULL,
    type          TINYINT      NOT NULL COMMENT '1私信 2评论提醒 3新帖通知 4系统',
    is_read       TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    ref_id        BIGINT       NULL COMMENT '关联帖子/评论ID',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_to_read (to_user_id, is_read),
    INDEX idx_conversation (from_user_id, to_user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛消息';

CREATE TABLE IF NOT EXISTS forum_material (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    file_url       VARCHAR(500) NOT NULL,
    subject        VARCHAR(32)  NULL COMMENT '学科',
    grade          VARCHAR(16)  NULL,
    user_id        BIGINT       NOT NULL,
    download_count INT          NOT NULL DEFAULT 0,
    status         TINYINT      NOT NULL DEFAULT 0,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subject_grade (subject, grade),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资料';

CREATE TABLE IF NOT EXISTS forum_like (
    id          BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞';

INSERT INTO forum_board (name, description, sort_order) VALUES
('选科交流', '3+1+2选科组合、科目搭配讨论', 1),
('志愿填报', '院校专业、冲稳保策略交流', 2),
('模考讨论', '一模二模、联考成绩与复习', 3),
('学习资料', '试卷、笔记、课件分享', 4)
ON DUPLICATE KEY UPDATE name = VALUES(name);
