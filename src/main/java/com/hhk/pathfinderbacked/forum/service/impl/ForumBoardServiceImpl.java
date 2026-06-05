package com.hhk.pathfinderbacked.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhk.pathfinderbacked.forum.entity.ForumBoard;
import com.hhk.pathfinderbacked.forum.mapper.ForumBoardMapper;
import com.hhk.pathfinderbacked.forum.service.ForumBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumBoardServiceImpl implements ForumBoardService {

    private final ForumBoardMapper forumBoardMapper;

    @Override
    public List<ForumBoard> listBoards() {
        return forumBoardMapper.selectList(new LambdaQueryWrapper<ForumBoard>()
                .eq(ForumBoard::getStatus, 0)
                .orderByAsc(ForumBoard::getSortOrder));
    }
}
