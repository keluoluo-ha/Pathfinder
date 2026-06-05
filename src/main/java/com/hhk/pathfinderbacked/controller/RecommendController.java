package com.hhk.pathfinderbacked.controller;

import com.hhk.pathfinderbacked.common.Result;
import com.hhk.pathfinderbacked.dto.RecommendRequest;
import com.hhk.pathfinderbacked.service.RecommendService;
import com.hhk.pathfinderbacked.service.ScoreRankService;
import com.hhk.pathfinderbacked.vo.RecommendResultVO;
import com.hhk.pathfinderbacked.vo.ScoreRankConvertVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final ScoreRankService scoreRankService;

    @PostMapping
    public Result<RecommendResultVO> recommend(@Valid @RequestBody RecommendRequest request) {
        return Result.success(recommendService.recommend(request));
    }

    @GetMapping("/score-rank/convert")
    public Result<ScoreRankConvertVO> convert(@RequestParam @NotNull Integer year,
                                              @RequestParam @NotNull Integer score,
                                              @RequestParam @NotNull @Min(1) @Max(2) Integer subjectType) {
        return Result.success(scoreRankService.convert(year, score, subjectType));
    }
}
