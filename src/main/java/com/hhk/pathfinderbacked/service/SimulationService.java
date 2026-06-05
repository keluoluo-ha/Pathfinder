package com.hhk.pathfinderbacked.service;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.dto.SimulationSaveRequest;
import com.hhk.pathfinderbacked.vo.SimulationDetailVO;
import com.hhk.pathfinderbacked.vo.SimulationRecordVO;

public interface SimulationService {
    Long save(SimulationSaveRequest request);

    PageResult<SimulationRecordVO> list(Long pageNo, Long pageSize);

    SimulationDetailVO detail(Long id);

    void delete(Long id);
}
