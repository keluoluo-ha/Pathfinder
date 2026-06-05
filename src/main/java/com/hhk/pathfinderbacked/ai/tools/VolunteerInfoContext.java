package com.hhk.pathfinderbacked.ai.tools;

import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.enums.BatchEnum;
import com.hhk.pathfinderbacked.enums.SubjectTypeEnum;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.utils.UserContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 合并对话入参与登录档案，判断志愿查询必填项是否齐全。
 */
@Getter
public class VolunteerInfoContext {

    private final Integer rankNo;
    private final Integer subjectType;
    private final Integer batch;
    private final Integer score;
    private final boolean batchConfirmed;
    private final List<String> missingFields = new ArrayList<>();

    private VolunteerInfoContext(Integer rankNo, Integer subjectType, Integer batch, Integer score, boolean batchConfirmed) {
        this.rankNo = rankNo;
        this.subjectType = subjectType;
        this.batch = batch;
        this.score = score;
        this.batchConfirmed = batchConfirmed;
        evaluateMissing();
    }

    public static VolunteerInfoContext resolve(Integer knownRankNo, Integer knownSubjectType, Integer knownBatch,
                                             Integer knownScore, StudentMapper studentMapper) {
        Integer rankNo = knownRankNo;
        Integer subjectType = knownSubjectType;
        Integer batch = knownBatch;
        Integer score = knownScore;
        boolean batchConfirmed = knownBatch != null && BatchEnum.isValid(knownBatch);

        Student student = loadStudent(studentMapper);
        if (student != null) {
            if (rankNo == null && student.getRankNo() != null && student.getRankNo() > 0) {
                rankNo = student.getRankNo();
            }
            if (subjectType == null && SubjectTypeEnum.isValid(student.getSubjectType())) {
                subjectType = student.getSubjectType();
            }
            if (score == null && student.getScore() != null) {
                score = student.getScore();
            }
        }

        if (!batchConfirmed) {
            batch = BatchEnum.UNDERGRADUATE.getCode();
        }

        return new VolunteerInfoContext(rankNo, subjectType, batch, score, batchConfirmed);
    }

    private static Student loadStudent(StudentMapper studentMapper) {
        Long userId = UserContext.getUserId();
        if (userId == null || studentMapper == null) {
            return null;
        }
        return studentMapper.selectById(userId);
    }

    private void evaluateMissing() {
        if (rankNo == null || rankNo <= 0) {
            missingFields.add("RANK_NO");
        }
        if (!SubjectTypeEnum.isValid(subjectType)) {
            missingFields.add("SUBJECT_TYPE");
        }
        if (!batchConfirmed) {
            missingFields.add("BATCH_CONFIRM");
        }
    }

    public boolean isReadyForRecommend() {
        return missingFields.isEmpty();
    }

    public String subjectTypeLabel() {
        SubjectTypeEnum type = SubjectTypeEnum.fromCode(subjectType);
        return type == null ? "未知" : type.getDesc();
    }

    public String batchLabel() {
        BatchEnum batchEnum = BatchEnum.fromCode(batch);
        return batchEnum == null ? "本科" : batchEnum.getDesc();
    }
}
