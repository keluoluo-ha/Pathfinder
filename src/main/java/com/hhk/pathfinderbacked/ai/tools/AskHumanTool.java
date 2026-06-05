package com.hhk.pathfinderbacked.ai.tools;

import com.hhk.pathfinderbacked.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 当志愿查询必填信息不足时，结构化提示 Agent 向用户追问。
 */
@Component
@RequiredArgsConstructor
public class AskHumanTool {

    private final StudentMapper studentMapper;

    @Tool(description = """
            Ask the user for missing volunteer planning information before querying admission database.
            Call this when the user mentions rank/score/volunteer help but subject type (physics/history) or batch is unclear.
            Do NOT call the admission database tool until status is READY or user has answered the questions.""")
    public String askUserForVolunteerInfo(
            @ToolParam(description = "User's rank number if mentioned in conversation, e.g. 5000", required = false) Integer knownRankNo,
            @ToolParam(description = "Subject type: 1=physics, 2=history, if known", required = false) Integer knownSubjectType,
            @ToolParam(description = "Batch: 1=undergraduate, 2=college, if known", required = false) Integer knownBatch,
            @ToolParam(description = "Gaokao score if mentioned", required = false) Integer knownScore) {

        VolunteerInfoContext ctx = VolunteerInfoContext.resolve(
                knownRankNo, knownSubjectType, knownBatch, knownScore, studentMapper);

        if (ctx.isReadyForRecommend()) {
            return buildReadyResponse(ctx);
        }
        return buildNeedInputResponse(ctx);
    }

    private String buildReadyResponse(VolunteerInfoContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("status: READY\n");
        sb.append("message: 志愿查询必填信息已齐全，请立即调用 queryVolunteerRecommendByRank 工具，勿重复追问。\n");
        sb.append("prefill:\n");
        sb.append("  rankNo: ").append(ctx.getRankNo()).append("\n");
        sb.append("  subjectType: ").append(ctx.getSubjectType())
                .append(" (").append(ctx.subjectTypeLabel()).append(")\n");
        sb.append("  batch: ").append(ctx.getBatch())
                .append(" (").append(ctx.batchLabel()).append(")\n");
        if (ctx.getScore() != null) {
            sb.append("  score: ").append(ctx.getScore()).append("\n");
        }
        return sb.toString();
    }

    private String buildNeedInputResponse(VolunteerInfoContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("status: NEED_USER_INPUT\n");
        sb.append("missing: ").append(String.join(", ", ctx.getMissingFields())).append("\n");

        if (ctx.getRankNo() != null) {
            sb.append("prefill:\n  rankNo: ").append(ctx.getRankNo()).append("\n");
        }
        if (ctx.getSubjectType() != null && !ctx.getMissingFields().contains("SUBJECT_TYPE")) {
            sb.append("  subjectType: ").append(ctx.getSubjectType())
                    .append(" (").append(ctx.subjectTypeLabel()).append(")\n");
        }
        if (ctx.getScore() != null) {
            sb.append("  score: ").append(ctx.getScore()).append("\n");
        }

        List<String> questions = buildQuestions(ctx);
        sb.append("questions:\n");
        for (int i = 0; i < questions.size(); i++) {
            sb.append(i + 1).append(". ").append(questions.get(i)).append("\n");
        }
        sb.append("hint: 请用中文向用户提出以上问题，本轮不要调用数据库推荐工具；用户回复后再调用 askUserForVolunteerInfo 或 queryVolunteerRecommendByRank。");
        return sb.toString();
    }

    private List<String> buildQuestions(VolunteerInfoContext ctx) {
        List<String> questions = new ArrayList<>();
        if (ctx.getMissingFields().contains("RANK_NO")) {
            questions.add("你的广东省高考全省位次（排名）是多少？");
        }
        if (ctx.getMissingFields().contains("SUBJECT_TYPE")) {
            questions.add("你选考的是物理类还是历史类？（物理类填1，历史类填2）");
        }
        if (ctx.getMissingFields().contains("BATCH_CONFIRM")) {
            questions.add("你想查询本科还是专科批次？（不说则默认按本科查询）");
        }
        if (questions.isEmpty()) {
            questions.add("请补充你的科类（物理类/历史类）和报考批次（本科/专科）。");
        }
        questions.add("（可选）有目标城市、院校或专业方向吗？补充后推荐解读会更精准。");
        return questions;
    }
}
