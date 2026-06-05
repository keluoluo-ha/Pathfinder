package com.hhk.pathfinderbacked.forum.service;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumMaterialCreateRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface ForumMaterialService {
    Long upload(Long userId, ForumMaterialCreateRequest request, MultipartFile file);

    PageResult<ForumMaterial> list(String subject, String grade, Long pageNo, Long pageSize);

    ForumMaterial detail(Long id);

    void increaseDownload(Long id);
}
