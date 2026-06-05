package com.hhk.pathfinderbacked.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumMaterialCreateRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumMaterial;
import com.hhk.pathfinderbacked.forum.mapper.ForumMaterialMapper;
import com.hhk.pathfinderbacked.forum.service.ForumMaterialService;
import com.hhk.pathfinderbacked.forum.support.ForumFileStorage;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumMaterialServiceImpl implements ForumMaterialService {

    private final ForumMaterialMapper forumMaterialMapper;
    private final ForumUserSupport forumUserSupport;
    private final ForumFileStorage forumFileStorage;

    @Override
    public Long upload(Long userId, ForumMaterialCreateRequest request, MultipartFile file) {
        forumUserSupport.requireStudent(userId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请上传文件");
        }
        try {
            forumFileStorage.ensureRootDir();
            Path dir = forumFileStorage.rootDir();
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            ForumMaterial material = new ForumMaterial();
            material.setTitle(request.getTitle());
            material.setFileUrl(forumFileStorage.toPublicUrl(filename));
            material.setSubject(request.getSubject());
            material.setGrade(request.getGrade());
            material.setUserId(userId);
            material.setDownloadCount(0);
            material.setStatus(0);
            forumMaterialMapper.insert(material);
            log.info("Forum material uploaded: id={}, path={}", material.getId(), target);
            return material.getId();
        } catch (IOException e) {
            log.error("Forum material upload failed, dir={}", forumFileStorage.rootDir(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public PageResult<ForumMaterial> list(String subject, String grade, Long pageNo, Long pageSize) {
        LambdaQueryWrapper<ForumMaterial> wrapper = new LambdaQueryWrapper<ForumMaterial>()
                .eq(ForumMaterial::getStatus, 0)
                .orderByDesc(ForumMaterial::getCreateTime);
        if (StringUtils.hasText(subject)) {
            wrapper.eq(ForumMaterial::getSubject, subject);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(ForumMaterial::getGrade, grade);
        }
        Page<ForumMaterial> page = forumMaterialMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), pageNo, pageSize, page.getRecords());
    }

    @Override
    public ForumMaterial detail(Long id) {
        ForumMaterial material = forumMaterialMapper.selectById(id);
        if (material == null || material.getStatus() != 0) {
            throw new BusinessException(ErrorCode.FORUM_MATERIAL_NOT_FOUND);
        }
        return material;
    }

    @Override
    public void increaseDownload(Long id) {
        ForumMaterial material = detail(id);
        material.setDownloadCount(material.getDownloadCount() + 1);
        forumMaterialMapper.updateById(material);
    }
}
