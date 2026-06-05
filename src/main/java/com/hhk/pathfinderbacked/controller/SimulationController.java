package com.hhk.pathfinderbacked.controller;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.common.Result;
import com.hhk.pathfinderbacked.dto.SimulationSaveRequest;
import com.hhk.pathfinderbacked.service.SimulationService;
import com.hhk.pathfinderbacked.vo.SimulationDetailVO;
import com.hhk.pathfinderbacked.vo.SimulationRecordVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/save")
    public Result<Long> save(@Valid @RequestBody SimulationSaveRequest request) {
        return Result.success(simulationService.save(request));
    }

    @GetMapping("/list")
    public Result<PageResult<SimulationRecordVO>> list(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                       @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return Result.success(simulationService.list(pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<SimulationDetailVO> detail(@PathVariable Long id) {
        return Result.success(simulationService.detail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        simulationService.delete(id);
        return Result.success();
    }
}
