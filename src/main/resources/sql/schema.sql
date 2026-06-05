-- =====================================
-- 数据库
-- =====================================
CREATE DATABASE IF NOT EXISTS pathfinder
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE pathfinder;

-- =====================================
-- 1. 院校表
-- =====================================
CREATE TABLE university (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
school_code VARCHAR(20) NOT NULL COMMENT '院校代码（唯一标识）',
school_name VARCHAR(255) NOT NULL COMMENT '院校全称',
short_name VARCHAR(100) DEFAULT NULL COMMENT '院校简称',
province VARCHAR(50) DEFAULT NULL COMMENT '院校所在省份',
city VARCHAR(50) DEFAULT NULL COMMENT '院校所在城市',
school_type VARCHAR(50) DEFAULT NULL COMMENT '院校类型（综合/理工/师范/医药等）',
level_tags VARCHAR(255) DEFAULT NULL COMMENT '院校标签（985,211,双一流等，逗号分隔）',
nature VARCHAR(50) DEFAULT NULL COMMENT '办学性质（公办/民办/中外合作）',
is_double_first_class TINYINT DEFAULT 0 COMMENT '是否双一流（0否 1是）',
is_985 TINYINT DEFAULT 0 COMMENT '是否985（0否 1是）',
is_211 TINYINT DEFAULT 0 COMMENT '是否211（0否 1是）',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (id),
UNIQUE KEY uk_school_code (school_code),
KEY idx_school_name (school_name),
KEY idx_province_city (province, city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校基础信息表';


-- =====================================
-- 2. 专业表
-- =====================================
CREATE TABLE major (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
major_code VARCHAR(50) DEFAULT NULL COMMENT '专业代码（教育部标准）',
major_name VARCHAR(255) NOT NULL COMMENT '专业名称',
major_category VARCHAR(100) DEFAULT NULL COMMENT '专业大类（工学/理学/文学等）',
degree_type VARCHAR(50) DEFAULT NULL COMMENT '学位类型（本科/专科/硕士等）',
duration_year INT DEFAULT 4 COMMENT '学制（单位：年）',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (id),
KEY idx_major_name (major_name),
KEY idx_major_category (major_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业信息表';


-- =====================================
-- 3. 核心录取数据表
-- =====================================
CREATE TABLE admission_data (
    id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    school_code  VARCHAR(50)  NULL COMMENT '学校代码',
    school_name  VARCHAR(200) NULL COMMENT '学校名称',
    group_code   VARCHAR(50)  NULL COMMENT '院校专业组代码',
    plan_count   VARCHAR(50)  NULL COMMENT '计划招生人数',
    enroll_count VARCHAR(50)  NULL COMMENT '实际录取人数',
    min_score    VARCHAR(50)  NULL COMMENT '最低录取分数',
    min_rank     VARCHAR(50)  NULL COMMENT '最低录取排位',
    source       VARCHAR(200) NULL COMMENT '数据来源',
    batch        VARCHAR(50)  NULL COMMENT '录取批次（提前批/本科/专科）',
    category     VARCHAR(50)  NULL COMMENT '科类（物理类/历史类等）',
    scrape_time  VARCHAR(50)  NULL COMMENT '数据采集时间',
    year         VARCHAR(20) DEFAULT '' NULL COMMENT '录取年份',
    CONSTRAINT uk_schl_grp_bat_cat UNIQUE (school_code, group_code, batch, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广东高考录取数据表';

CREATE INDEX idx_school_code ON admission_data (school_code);
CREATE INDEX idx_school_name ON admission_data (school_name);


-- =====================================
-- 4. 学生表
-- =====================================
CREATE TABLE student (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
student_no VARCHAR(50) DEFAULT NULL COMMENT '学生编号（系统生成）',
name VARCHAR(100) NOT NULL COMMENT '学生姓名',
avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
signature VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
mobile VARCHAR(20) DEFAULT NULL COMMENT '手机号（登录账号）',
password VARCHAR(255) DEFAULT NULL COMMENT '登录密码（加密存储）',
star_coin INT NOT NULL DEFAULT 0 COMMENT '我的星币',
balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '我的余额（元）',
unread_message_count INT NOT NULL DEFAULT 0 COMMENT '我的消息-未读数',
exchange_record_count INT NOT NULL DEFAULT 0 COMMENT '兑换记录总数',
volunteer_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '我的志愿时（小时）',
planning_contact_phone VARCHAR(30) DEFAULT NULL COMMENT '学涯规划一对一联系电话',

    subject_type TINYINT NOT NULL COMMENT '科类（1物理类 2历史类）',
    score INT NOT NULL COMMENT '高考成绩',
    rank_no INT NOT NULL COMMENT '全省排名',

    province VARCHAR(50) DEFAULT '广东' COMMENT '所在省份',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_mobile (mobile),
    KEY idx_nickname (nickname),
    KEY idx_rank (rank_no),
    KEY idx_score (score),
    KEY idx_star_coin (star_coin),
    KEY idx_balance (balance),
    KEY idx_volunteer_hours (volunteer_hours)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';


-- =====================================
-- 5. 志愿模拟记录表
-- =====================================
CREATE TABLE volunteer_simulation_record (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
student_id BIGINT NOT NULL COMMENT '学生ID（逻辑关联student）',
score INT NOT NULL COMMENT '模拟时使用的分数',
rank_no INT NOT NULL COMMENT '模拟时使用的排名',
subject_type TINYINT NOT NULL COMMENT '科类（1物理类 2历史类）',
batch TINYINT NOT NULL COMMENT '批次（1本科 2专科）',
simulation_name VARCHAR(100) DEFAULT NULL COMMENT '模拟名称（用户自定义）',
strategy_type TINYINT DEFAULT 1 COMMENT '策略类型（1冲稳保）',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (id),
KEY idx_student_id (student_id),
KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿模拟记录表';


-- =====================================
-- 6. 志愿模拟详情表
-- =====================================
CREATE TABLE volunteer_simulation_detail (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
simulation_id BIGINT NOT NULL COMMENT '模拟记录ID（逻辑关联record）',
volunteer_order INT NOT NULL COMMENT '志愿顺序（第1志愿、第2志愿等）',
school_code VARCHAR(20) NOT NULL COMMENT '院校代码',
school_name VARCHAR(255) NOT NULL COMMENT '院校名称',
major_name VARCHAR(255) NOT NULL COMMENT '专业名称',
predicted_probability DECIMAL(5,2) DEFAULT NULL COMMENT '预测录取概率（0-100）',
risk_level TINYINT DEFAULT NULL COMMENT '风险等级（1冲 2稳 3保）',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (id),
KEY idx_simulation_id (simulation_id),
KEY idx_school_code (school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿填报明细表';


-- =====================================
-- 7. 分数-排名映射表
-- =====================================
CREATE TABLE score_rank_mapping (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
year INT NOT NULL COMMENT '年份',
subject_type TINYINT NOT NULL COMMENT '科类（1物理类 2历史类）',
score INT NOT NULL COMMENT '分数',
rank_no INT NOT NULL COMMENT '对应排名',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (id),
UNIQUE KEY uk_score_rank (year, subject_type, score),
KEY idx_rank_no (year, subject_type, rank_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分数与排名映射表';


-- =====================================
-- 8. 推荐缓存表
-- =====================================
CREATE TABLE recommendation_cache (
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
score INT NOT NULL COMMENT '用户分数',
rank_no INT NOT NULL COMMENT '用户排名',
subject_type TINYINT NOT NULL COMMENT '科类（1物理类 2历史类）',
batch TINYINT NOT NULL COMMENT '批次（1本科 2专科）',
cache_key VARCHAR(100) NOT NULL COMMENT '缓存唯一键（组合条件生成）',
result_json JSON NOT NULL COMMENT '推荐结果（JSON格式）',
expire_time DATETIME NOT NULL COMMENT '缓存过期时间',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (id),
UNIQUE KEY uk_cache_key (cache_key),
KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿推荐结果缓存表';
