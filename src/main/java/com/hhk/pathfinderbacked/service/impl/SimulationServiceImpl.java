package com.hhk.pathfinderbacked.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.dto.SimulationDetailItemDTO;
import com.hhk.pathfinderbacked.dto.SimulationSaveRequest;
import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.entity.VolunteerSimulationDetail;
import com.hhk.pathfinderbacked.entity.VolunteerSimulationRecord;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.mapper.VolunteerSimulationDetailMapper;
import com.hhk.pathfinderbacked.mapper.VolunteerSimulationRecordMapper;
import com.hhk.pathfinderbacked.service.SimulationService;
import com.hhk.pathfinderbacked.utils.UserContext;
import com.hhk.pathfinderbacked.vo.SimulationDetailItemVO;
import com.hhk.pathfinderbacked.vo.SimulationDetailVO;
import com.hhk.pathfinderbacked.vo.SimulationRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationServiceImpl implements SimulationService {

    private final VolunteerSimulationRecordMapper recordMapper;
    private final VolunteerSimulationDetailMapper detailMapper;
    private final StudentMapper studentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(SimulationSaveRequest request) {
        Student student = getCurrentStudent();
        VolunteerSimulationRecord record = new VolunteerSimulationRecord();
        record.setStudentId(student.getId());
        record.setScore(request.getScore());
        record.setRankNo(request.getRankNo());
        record.setSubjectType(student.getSubjectType());
        record.setBatch(request.getBatch());
        record.setSimulationName(request.getSimulationName());
        record.setStrategyType(1);
        recordMapper.insert(record);

        for (SimulationDetailItemDTO dto : request.getDetails()) {
            VolunteerSimulationDetail detail = new VolunteerSimulationDetail();
            detail.setSimulationId(record.getId());
            detail.setVolunteerOrder(dto.getVolunteerOrder());
            detail.setSchoolCode(dto.getSchoolCode());
            detail.setSchoolName(dto.getSchoolName());
            detail.setMajorName(dto.getMajorName());
            detail.setPredictedProbability(dto.getPredictedProbability());
            detail.setRiskLevel(dto.getRiskLevel());
            detailMapper.insert(detail);
        }
        return record.getId();
    }

    @Override
    public PageResult<SimulationRecordVO> list(Long pageNo, Long pageSize) {
        Long userId = requireUserId();
        Page<VolunteerSimulationRecord> page = new Page<>(pageNo, pageSize);
        Page<VolunteerSimulationRecord> resultPage = recordMapper.selectPage(page, new LambdaQueryWrapper<VolunteerSimulationRecord>()
                .eq(VolunteerSimulationRecord::getStudentId, userId)
                .orderByDesc(VolunteerSimulationRecord::getCreateTime));
        List<SimulationRecordVO> records = resultPage.getRecords().stream()
                .map(it -> BeanUtil.copyProperties(it, SimulationRecordVO.class))
                .toList();
        return new PageResult<>(resultPage.getTotal(), pageNo, pageSize, records);
    }

    @Override
    public SimulationDetailVO detail(Long id) {
        VolunteerSimulationRecord record = getOwnedRecord(id);
        List<VolunteerSimulationDetail> details = detailMapper.selectList(new LambdaQueryWrapper<VolunteerSimulationDetail>()
                .eq(VolunteerSimulationDetail::getSimulationId, id)
                .orderByAsc(VolunteerSimulationDetail::getVolunteerOrder));
        List<SimulationDetailItemVO> detailItems = details.stream()
                .map(it -> BeanUtil.copyProperties(it, SimulationDetailItemVO.class))
                .toList();
        SimulationDetailVO vo = new SimulationDetailVO();
        vo.setRecord(BeanUtil.copyProperties(record, SimulationRecordVO.class));
        vo.setDetails(detailItems);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getOwnedRecord(id);
        detailMapper.delete(new LambdaQueryWrapper<VolunteerSimulationDetail>()
                .eq(VolunteerSimulationDetail::getSimulationId, id));
        recordMapper.deleteById(id);
    }

    private VolunteerSimulationRecord getOwnedRecord(Long id) {
        Long userId = requireUserId();
        VolunteerSimulationRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.SIMULATION_NOT_FOUND);
        }
        if (!record.getStudentId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return record;
    }

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private Student getCurrentStudent() {
        Long userId = requireUserId();
        Student student = studentMapper.selectById(userId);
        if (student == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return student;
    }
}
