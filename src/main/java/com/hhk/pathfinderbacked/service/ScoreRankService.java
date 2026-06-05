package com.hhk.pathfinderbacked.service;

import com.hhk.pathfinderbacked.vo.ScoreRankConvertVO;

public interface ScoreRankService {
    ScoreRankConvertVO convert(Integer year, Integer score, Integer subjectType);
}
