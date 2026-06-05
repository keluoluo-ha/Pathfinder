package com.hhk.pathfinderbacked.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhk.pathfinderbacked.cache.CacheConfig;
import com.hhk.pathfinderbacked.cache.CacheService;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.config.RecommendationProperties;
import com.hhk.pathfinderbacked.converter.AdmissionDataConverter;
import com.hhk.pathfinderbacked.dto.RecommendRequest;
import com.hhk.pathfinderbacked.entity.AdmissionData;
import com.hhk.pathfinderbacked.entity.RecommendationCache;
import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.enums.BatchEnum;
import com.hhk.pathfinderbacked.enums.RiskLevelEnum;
import com.hhk.pathfinderbacked.enums.SubjectTypeEnum;
import com.hhk.pathfinderbacked.mapper.AdmissionDataMapper;
import com.hhk.pathfinderbacked.mapper.RecommendationCacheMapper;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.service.RecommendService;
import com.hhk.pathfinderbacked.utils.UserContext;
import com.hhk.pathfinderbacked.vo.RecommendItemVO;
import com.hhk.pathfinderbacked.vo.RecommendResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final BigDecimal CHONG_THRESHOLD = new BigDecimal("1.15");

    private final StudentMapper studentMapper;
    private final AdmissionDataMapper admissionDataMapper;
    private final RecommendationCacheMapper recommendationCacheMapper;
    private final RecommendationProperties recommendationProperties;
    private final CacheService cacheService;

    @Override
    public RecommendResultVO recommend(RecommendRequest request) {
        Student student = getCurrentStudent();
        return recommendByRank(
                request.getRankNo(),
                request.getBatch(),
                student.getSubjectType(),
                request.getChongLimit(),
                request.getWenLimit(),
                request.getBaoLimit());
    }

    @Override
    public RecommendResultVO recommendByRank(Integer rankNo, Integer batch, Integer subjectType,
                                             Integer chongLimit, Integer wenLimit, Integer baoLimit) {
        if (rankNo == null || rankNo <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "位次无效");
        }
        if (!SubjectTypeEnum.isValid(subjectType)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "科类无效，需为1(物理类)或2(历史类)");
        }
        int resolvedBatch = batch == null ? BatchEnum.UNDERGRADUATE.getCode() : batch;
        if (!BatchEnum.isValid(resolvedBatch)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "批次无效，需为1(本科)或2(专科)");
        }
        int chong = chongLimit == null ? recommendationProperties.getChongLimit() : chongLimit;
        int wen = wenLimit == null ? recommendationProperties.getWenLimit() : wenLimit;
        int bao = baoLimit == null ? recommendationProperties.getBaoLimit() : baoLimit;

        String cacheKey = buildCacheKey(rankNo, resolvedBatch, subjectType, chong, wen, bao);
        String redisJson = cacheService.get(cacheKey);
        if (redisJson != null) {
            return JSONUtil.toBean(redisJson, RecommendResultVO.class);
        }

        RecommendationCache dbCache = recommendationCacheMapper.selectOne(new LambdaQueryWrapper<RecommendationCache>()
                .eq(RecommendationCache::getCacheKey, cacheKey)
                .gt(RecommendationCache::getExpireTime, LocalDateTime.now())
                .last("limit 1"));
        if (dbCache != null) {
            cacheService.set(cacheKey, dbCache.getResultJson(), Duration.ofMinutes(recommendationProperties.getRedisTtlMinutes()));
            return JSONUtil.toBean(dbCache.getResultJson(), RecommendResultVO.class);
        }

        RecommendResultVO resultVO = calculateByRank(rankNo, resolvedBatch, subjectType, chong, wen, bao);
        String resultJson = JSONUtil.toJsonStr(resultVO);
        cacheService.set(cacheKey, resultJson, Duration.ofMinutes(recommendationProperties.getRedisTtlMinutes()));

        Integer scoreForCache = resolveScoreForCache();
        upsertDbCache(scoreForCache, rankNo, subjectType, resolvedBatch, cacheKey, resultJson);
        return resultVO;
    }

    private Integer resolveScoreForCache() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return 0;
        }
        Student student = studentMapper.selectById(userId);
        return student == null || student.getScore() == null ? 0 : student.getScore();
    }

    private RecommendResultVO calculateByRank(Integer rankNo, Integer batch, Integer subjectType,
                                              Integer chongLimit, Integer wenLimit, Integer baoLimit) {
        String category = AdmissionDataConverter.resolveCategory(subjectType);
        String batchLabel = AdmissionDataConverter.resolveBatchLabel(batch);
        List<String> recentYears = AdmissionDataConverter.recentThreeYears();

        List<AdmissionData> rows = admissionDataMapper.selectList(new LambdaQueryWrapper<AdmissionData>()
                .eq(AdmissionData::getCategory, category)
                .eq(AdmissionData::getBatch, batchLabel)
                .in(AdmissionData::getYear, recentYears)
                .isNotNull(AdmissionData::getMinRank)
                .ne(AdmissionData::getMinRank, ""));
        if (rows.isEmpty()) {
            return new RecommendResultVO();
        }

        Map<String, AdmissionData> dedupMap = new LinkedHashMap<>();
        for (AdmissionData row : rows) {
            if (!AdmissionDataConverter.isValidForRecommend(row)) {
                continue;
            }
            String key = row.getSchoolCode() + "|" + row.getGroupCode();
            AdmissionData exist = dedupMap.get(key);
            Integer rowYear = AdmissionDataConverter.parseYear(row.getYear());
            Integer existYear = exist == null ? null : AdmissionDataConverter.parseYear(exist.getYear());
            if (exist == null || (rowYear != null && existYear != null && rowYear > existYear)) {
                dedupMap.put(key, row);
            }
        }
        List<RecommendItemVO> allItems = dedupMap.values().stream()
                .map(it -> AdmissionDataConverter.toRecommendItem(it, rankNo))
                .filter(item -> item.getRatio().compareTo(CHONG_THRESHOLD) <= 0)
                .toList();

        RecommendResultVO result = new RecommendResultVO();
        result.setBao(allItems.stream()
                .filter(it -> it.getRiskLevel().equals(RiskLevelEnum.BAO.getCode()))
                .sorted(Comparator.comparing(RecommendItemVO::getRatio).reversed())
                .limit(baoLimit)
                .collect(Collectors.toList()));
        result.setWen(allItems.stream()
                .filter(it -> it.getRiskLevel().equals(RiskLevelEnum.WEN.getCode()))
                .sorted(Comparator.comparing(it -> it.getRatio().subtract(BigDecimal.ONE).abs()))
                .limit(wenLimit)
                .collect(Collectors.toList()));
        result.setChong(allItems.stream()
                .filter(it -> it.getRiskLevel().equals(RiskLevelEnum.CHONG.getCode()))
                .sorted(Comparator.comparing(RecommendItemVO::getRatio))
                .limit(chongLimit)
                .collect(Collectors.toList()));
        return result;
    }

    private void upsertDbCache(Integer score, Integer rankNo, Integer subjectType, Integer batch, String cacheKey, String resultJson) {
        RecommendationCache dbCache = recommendationCacheMapper.selectOne(new LambdaQueryWrapper<RecommendationCache>()
                .eq(RecommendationCache::getCacheKey, cacheKey)
                .last("limit 1"));
        if (dbCache == null) {
            dbCache = new RecommendationCache();
            dbCache.setCacheKey(cacheKey);
            dbCache.setCreateTime(LocalDateTime.now());
        }
        dbCache.setScore(score == null ? 0 : score);
        dbCache.setRankNo(rankNo);
        dbCache.setSubjectType(subjectType);
        dbCache.setBatch(batch);
        dbCache.setResultJson(resultJson);
        dbCache.setExpireTime(LocalDateTime.now().plusHours(recommendationProperties.getDbCacheHours()));
        if (dbCache.getId() == null) {
            recommendationCacheMapper.insert(dbCache);
        } else {
            recommendationCacheMapper.updateById(dbCache);
        }
    }

    private String buildCacheKey(Integer rankNo, Integer batch, Integer subjectType, Integer chong, Integer wen, Integer bao) {
        return CacheConfig.RECOMMEND_PREFIX + subjectType + ":" + batch + ":" + rankNo + ":" + chong + ":" + wen + ":" + bao;
    }

    private Student getCurrentStudent() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return student;
    }
}
