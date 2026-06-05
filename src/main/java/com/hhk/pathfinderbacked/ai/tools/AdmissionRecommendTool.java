package com.hhk.pathfinderbacked.ai.tools;

import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.enums.SubjectTypeEnum;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.service.RecommendService;
import com.hhk.pathfinderbacked.vo.RecommendResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 按位次从 admission_data 查询冲稳保，与 HTTP 推荐接口逻辑一致。
 */
@Component
@RequiredArgsConstructor
public class AdmissionRecommendTool {

    private final RecommendService recommendService;
    private final StudentMapper studentMapper;

    @Tool(description = """
            Query Guangdong gaokao admission_data for chong/wen/bao volunteer recommendations by rank.
            Required: rankNo and subjectType (1=physics, 2=history). Batch defaults to 1 (undergraduate).
            Call askUserForVolunteerInfo first if subject type or rank is missing.
            Only use returned schools in your answer; do not invent schools.""")
    public String queryVolunteerRecommendByRank(
            @ToolParam(description = "User's provincial rank number, required") Integer rankNo,
            @ToolParam(description = "Subject type: 1=physics, 2=history, required") Integer subjectType,
            @ToolParam(description = "Batch: 1=undergraduate, 2=college, default 1", required = false) Integer batch,
            @ToolParam(description = "Max chong tier results, optional", required = false) Integer chongLimit,
            @ToolParam(description = "Max wen tier results, optional", required = false) Integer wenLimit,
            @ToolParam(description = "Max bao tier results, optional", required = false) Integer baoLimit) {

        VolunteerInfoContext ctx = VolunteerInfoContext.resolve(rankNo, subjectType, batch, null, studentMapper);
        if (!ctx.isReadyForRecommend()) {
            return "status: ERROR\n"
                    + "message: 缺少必填参数，请先调用 askUserForVolunteerInfo 向用户确认："
                    + String.join("、", ctx.getMissingFields()) + "\n"
                    + "hint: 不要编造院校名单。";
        }

        if (!SubjectTypeEnum.isValid(ctx.getSubjectType())) {
            return "status: ERROR\nmessage: 科类无效，需为1(物理类)或2(历史类)。\nhint: 请先调用 askUserForVolunteerInfo。";
        }

        try {
            RecommendResultVO result = recommendService.recommendByRank(
                    ctx.getRankNo(),
                    ctx.getBatch(),
                    ctx.getSubjectType(),
                    chongLimit,
                    wenLimit,
                    baoLimit);
            return RecommendResultFormatter.format(ctx.getRankNo(), ctx.getSubjectType(), ctx.getBatch(), result);
        } catch (BusinessException ex) {
            return "status: ERROR\nmessage: " + ex.getMessage() + "\nhint: 请检查参数或引导用户补充信息。";
        } catch (Exception ex) {
            return "status: ERROR\nmessage: 志愿数据查询失败：" + ex.getMessage();
        }
    }
}
