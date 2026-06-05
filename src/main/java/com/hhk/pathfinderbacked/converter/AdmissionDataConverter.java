package com.hhk.pathfinderbacked.converter;

import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.entity.AdmissionData;
import com.hhk.pathfinderbacked.enums.BatchEnum;
import com.hhk.pathfinderbacked.enums.RiskLevelEnum;
import com.hhk.pathfinderbacked.enums.SubjectTypeEnum;
import com.hhk.pathfinderbacked.vo.RecommendItemVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public final class AdmissionDataConverter {

    private static final int MIN_DATA_YEAR = 2022;
    private static final int MAX_DATA_YEAR = 2026;
    private static final BigDecimal BAO_THRESHOLD = new BigDecimal("0.85");
    private static final BigDecimal WEN_THRESHOLD = new BigDecimal("1.00");

    private AdmissionDataConverter() {
    }

    public static String resolveCategory(Integer subjectType) {
        SubjectTypeEnum type = SubjectTypeEnum.fromCode(subjectType);
        if (type == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "科类参数无效");
        }
        return type.getDesc();
    }

    public static String resolveBatchLabel(Integer batch) {
        BatchEnum batchEnum = BatchEnum.fromCode(batch);
        if (batchEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "批次参数无效");
        }
        return batchEnum.getDesc();
    }

    /** 最近三年（相对当前自然年，并限制在 2022–2026 数据范围内） */
    public static List<String> recentThreeYears() {
        int endYear = Math.min(Year.now().getValue(), MAX_DATA_YEAR);
        int startYear = Math.max(endYear - 2, MIN_DATA_YEAR);
        List<String> years = new ArrayList<>(3);
        for (int y = startYear; y <= endYear; y++) {
            years.add(String.valueOf(y));
        }
        return years;
    }

    public static Integer parseYear(String year) {
        if (year == null || year.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(year.trim());
            if (value < MIN_DATA_YEAR || value > MAX_DATA_YEAR) {
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Integer parseMinRank(String minRank) {
        if (minRank == null || minRank.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(minRank.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static boolean isValidForRecommend(AdmissionData row) {
        return parseMinRank(row.getMinRank()) != null && parseYear(row.getYear()) != null;
    }

    public static RecommendItemVO toRecommendItem(AdmissionData data, Integer rankNo) {
        Integer minRank = parseMinRank(data.getMinRank());
        BigDecimal ratio = BigDecimal.valueOf(rankNo)
                .divide(BigDecimal.valueOf(minRank), 4, RoundingMode.HALF_UP);
        int riskLevel;
        if (ratio.compareTo(BAO_THRESHOLD) <= 0) {
            riskLevel = RiskLevelEnum.BAO.getCode();
        } else if (ratio.compareTo(WEN_THRESHOLD) <= 0) {
            riskLevel = RiskLevelEnum.WEN.getCode();
        } else {
            riskLevel = RiskLevelEnum.CHONG.getCode();
        }

        RecommendItemVO itemVO = new RecommendItemVO();
        itemVO.setSchoolCode(data.getSchoolCode());
        itemVO.setSchoolName(data.getSchoolName());
        itemVO.setMajorName(data.getGroupCode());
        itemVO.setYear(parseYear(data.getYear()));
        itemVO.setMinRank(minRank);
        itemVO.setRatio(ratio);
        itemVO.setRiskLevel(riskLevel);
        itemVO.setPredictedProbability(calcProbability(ratio, riskLevel));
        return itemVO;
    }

    private static BigDecimal calcProbability(BigDecimal ratio, int riskLevel) {
        if (riskLevel == RiskLevelEnum.BAO.getCode()) {
            return new BigDecimal("85.00");
        }
        if (riskLevel == RiskLevelEnum.WEN.getCode()) {
            return new BigDecimal("65.00");
        }
        BigDecimal diff = ratio.subtract(WEN_THRESHOLD);
        BigDecimal base = new BigDecimal("55.00");
        BigDecimal penalty = diff.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal value = base.subtract(penalty);
        if (value.compareTo(new BigDecimal("30.00")) < 0) {
            value = new BigDecimal("30.00");
        }
        return value;
    }
}
