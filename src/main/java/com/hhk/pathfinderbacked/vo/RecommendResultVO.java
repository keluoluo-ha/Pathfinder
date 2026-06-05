package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendResultVO {
    private List<RecommendItemVO> chong = new ArrayList<>();
    private List<RecommendItemVO> wen = new ArrayList<>();
    private List<RecommendItemVO> bao = new ArrayList<>();
}
