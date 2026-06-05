package com.hhk.pathfinderbacked.ai.tools;

import com.hhk.pathfinderbacked.converter.AdmissionDataConverter;
import com.hhk.pathfinderbacked.enums.BatchEnum;
import com.hhk.pathfinderbacked.enums.RiskLevelEnum;
import com.hhk.pathfinderbacked.enums.SubjectTypeEnum;
import com.hhk.pathfinderbacked.vo.RecommendItemVO;
import com.hhk.pathfinderbacked.vo.RecommendResultVO;

import java.util.List;

public final class RecommendResultFormatter {

    private static final int DISPLAY_LIMIT = 8;

    private RecommendResultFormatter() {
    }

    public static String format(Integer rankNo, Integer subjectType, Integer batch, RecommendResultVO result) {
        StringBuilder sb = new StringBuilder();
        SubjectTypeEnum type = SubjectTypeEnum.fromCode(subjectType);
        BatchEnum batchEnum = BatchEnum.fromCode(batch);
        sb.append("status: SUCCESS\n");
        sb.append("query: 位次=").append(rankNo)
                .append(", 科类=").append(type == null ? subjectType : type.getDesc())
                .append(", 批次=").append(batchEnum == null ? batch : batchEnum.getDesc())
                .append(", 数据年份=").append(String.join("/", AdmissionDataConverter.recentThreeYears()))
                .append("\n");
        sb.append("rule: 位次比=你的位次/院校最低位次；保≤0.85，稳≤1.00，冲>1.00且≤1.15\n\n");

        appendTier(sb, "【保】", result.getBao(), RiskLevelEnum.BAO.getDesc());
        appendTier(sb, "【稳】", result.getWen(), RiskLevelEnum.WEN.getDesc());
        appendTier(sb, "【冲】", result.getChong(), RiskLevelEnum.CHONG.getDesc());

        int total = safeSize(result.getBao()) + safeSize(result.getWen()) + safeSize(result.getChong());
        if (total == 0) {
            sb.append("说明: 未匹配到符合条件的院校专业组，可尝试调整批次或确认位次、科类是否正确。\n");
        }
        sb.append("hint: 仅根据以上数据解读，勿编造未列出的院校。");
        return sb.toString();
    }

    private static void appendTier(StringBuilder sb, String title, List<RecommendItemVO> items, String tierName) {
        sb.append(title).append(" 共").append(safeSize(items)).append("条");
        if (items == null || items.isEmpty()) {
            sb.append("（暂无）\n\n");
            return;
        }
        sb.append("，展示前").append(Math.min(DISPLAY_LIMIT, items.size())).append("条：\n");
        int limit = Math.min(DISPLAY_LIMIT, items.size());
        for (int i = 0; i < limit; i++) {
            RecommendItemVO item = items.get(i);
            sb.append(i + 1).append(". ")
                    .append(nullToDash(item.getSchoolName()))
                    .append(" | 专业组=").append(nullToDash(item.getMajorName()))
                    .append(" | 最低位次=").append(item.getMinRank())
                    .append(" | 位次比=").append(item.getRatio())
                    .append(" | ").append(tierName)
                    .append(" | 参考概率=").append(item.getPredictedProbability()).append("%\n");
        }
        sb.append("\n");
    }

    private static int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
