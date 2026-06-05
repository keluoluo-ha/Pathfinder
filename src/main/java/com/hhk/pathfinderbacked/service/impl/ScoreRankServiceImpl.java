package com.hhk.pathfinderbacked.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.entity.ScoreRankMapping;
import com.hhk.pathfinderbacked.mapper.ScoreRankMappingMapper;
import com.hhk.pathfinderbacked.service.ScoreRankService;
import com.hhk.pathfinderbacked.vo.ScoreRankConvertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoreRankServiceImpl implements ScoreRankService {

    private final ScoreRankMappingMapper scoreRankMappingMapper;

    @Override
    public ScoreRankConvertVO convert(Integer year, Integer score, Integer subjectType) {
        ScoreRankMapping mapping = scoreRankMappingMapper.selectOne(new LambdaQueryWrapper<ScoreRankMapping>()
                .eq(ScoreRankMapping::getYear, year)
                .eq(ScoreRankMapping::getSubjectType, subjectType)
                .eq(ScoreRankMapping::getScore, score)
                .last("limit 1"));
        if (mapping == null) {
            throw new BusinessException(ErrorCode.SCORE_MAPPING_NOT_FOUND);
        }
        return new ScoreRankConvertVO(year, score, mapping.getRankNo());
    }
}
