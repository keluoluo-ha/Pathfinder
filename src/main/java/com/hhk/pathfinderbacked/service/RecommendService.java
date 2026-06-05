package com.hhk.pathfinderbacked.service;

import com.hhk.pathfinderbacked.dto.RecommendRequest;
import com.hhk.pathfinderbacked.vo.RecommendResultVO;

public interface RecommendService {
    RecommendResultVO recommend(RecommendRequest request);

    /**
     * 按位次、科类、批次查询冲稳保（供 AI Tool 等场景复用，与 HTTP 推荐计算逻辑一致）。
     */
    RecommendResultVO recommendByRank(Integer rankNo, Integer batch, Integer subjectType,
                                    Integer chongLimit, Integer wenLimit, Integer baoLimit);
}
