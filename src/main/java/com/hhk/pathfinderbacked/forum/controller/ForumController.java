package com.hhk.pathfinderbacked.forum.controller;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.common.Result;
import com.hhk.pathfinderbacked.forum.dto.ForumCommentCreateRequest;
import com.hhk.pathfinderbacked.forum.dto.ForumMaterialCreateRequest;
import com.hhk.pathfinderbacked.forum.dto.ForumPostCreateRequest;
import com.hhk.pathfinderbacked.forum.dto.ForumPrivateMessageRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumBoard;
import com.hhk.pathfinderbacked.forum.entity.ForumMaterial;
import com.hhk.pathfinderbacked.forum.service.ForumBoardService;
import com.hhk.pathfinderbacked.forum.service.ForumCommentService;
import com.hhk.pathfinderbacked.forum.service.ForumLikeService;
import com.hhk.pathfinderbacked.forum.service.ForumMaterialService;
import com.hhk.pathfinderbacked.forum.service.ForumMessageService;
import com.hhk.pathfinderbacked.forum.service.ForumPostService;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import com.hhk.pathfinderbacked.forum.vo.ForumCommentVO;
import com.hhk.pathfinderbacked.forum.vo.ForumMessageVO;
import com.hhk.pathfinderbacked.forum.vo.ForumPostVO;
import com.hhk.pathfinderbacked.forum.vo.ForumUnreadCountVO;
import com.hhk.pathfinderbacked.forum.websocket.WsSessionManager;
import com.hhk.pathfinderbacked.mapper.StudentMapper;
import com.hhk.pathfinderbacked.entity.Student;
import com.hhk.pathfinderbacked.forum.vo.ForumOnlineUserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumBoardService forumBoardService;
    private final ForumPostService forumPostService;
    private final ForumCommentService forumCommentService;
    private final ForumLikeService forumLikeService;
    private final ForumMaterialService forumMaterialService;
    private final ForumMessageService forumMessageService;
    private final ForumUserSupport forumUserSupport;
    private final WsSessionManager wsSessionManager;
    private final StudentMapper studentMapper;

    @GetMapping("/boards")
    public Result<List<ForumBoard>> boards() {
        return Result.success(forumBoardService.listBoards());
    }

    @PostMapping("/posts")
    public Result<Long> createPost(@Valid @RequestBody ForumPostCreateRequest request) {
        Long userId = forumUserSupport.requireUserId();
        return Result.success(forumPostService.createPost(userId, request));
    }

    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> listPosts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer gaokaoType,
            @RequestParam(required = false) String subjects,
            @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
            @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        Long userId = forumUserSupport.requireUserId();
        return Result.success(forumPostService.listPosts(boardId, category, grade, gaokaoType, subjects,
                pageNo, pageSize, userId));
    }

    @GetMapping("/posts/{id}")
    public Result<ForumPostVO> postDetail(@PathVariable Long id) {
        Long userId = forumUserSupport.requireUserId();
        return Result.success(forumPostService.detail(id, userId));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        forumPostService.deletePost(id, forumUserSupport.requireUserId());
        return Result.success();
    }

    @GetMapping("/posts/{id}/comments")
    public Result<PageResult<ForumCommentVO>> listComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
            @RequestParam(defaultValue = "20") @Min(1) Long pageSize) {
        return Result.success(forumCommentService.listComments(id, pageNo, pageSize));
    }

    @PostMapping("/posts/{id}/comments")
    public Result<Long> addComment(@PathVariable Long id, @Valid @RequestBody ForumCommentCreateRequest request) {
        return Result.success(forumCommentService.addComment(id, forumUserSupport.requireUserId(), request));
    }

    @PostMapping("/posts/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        forumLikeService.like(id, forumUserSupport.requireUserId());
        return Result.success();
    }

    @DeleteMapping("/posts/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        forumLikeService.unlike(id, forumUserSupport.requireUserId());
        return Result.success();
    }

    @PostMapping(value = "/materials", consumes = "multipart/form-data")
    public Result<Long> uploadMaterial(
            @Valid @ModelAttribute ForumMaterialCreateRequest request,
            @RequestParam("file") MultipartFile file) {
        return Result.success(forumMaterialService.upload(forumUserSupport.requireUserId(), request, file));
    }

    @GetMapping("/materials")
    public Result<PageResult<ForumMaterial>> listMaterials(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
            @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return Result.success(forumMaterialService.list(subject, grade, pageNo, pageSize));
    }

    @GetMapping("/materials/{id}")
    public Result<ForumMaterial> materialDetail(@PathVariable Long id) {
        return Result.success(forumMaterialService.detail(id));
    }

    @PostMapping("/materials/{id}/download")
    public Result<ForumMaterial> downloadMaterial(@PathVariable Long id) {
        forumMaterialService.increaseDownload(id);
        return Result.success(forumMaterialService.detail(id));
    }

    @PostMapping("/messages/private")
    public Result<Void> sendPrivate(@Valid @RequestBody ForumPrivateMessageRequest request) {
        forumMessageService.sendPrivateMessage(forumUserSupport.requireUserId(), request);
        return Result.success();
    }

    @GetMapping("/messages")
    public Result<PageResult<ForumMessageVO>> messages(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long peerUserId,
            @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
            @RequestParam(defaultValue = "20") @Min(1) Long pageSize) {
        return Result.success(forumMessageService.listMessages(
                forumUserSupport.requireUserId(), type, peerUserId, pageNo, pageSize));
    }

    @GetMapping("/messages/unread-count")
    public Result<ForumUnreadCountVO> unreadCount() {
        return Result.success(forumMessageService.unreadCount(forumUserSupport.requireUserId()));
    }

    @PutMapping("/messages/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        forumMessageService.markRead(forumUserSupport.requireUserId(), id);
        return Result.success();
    }

    @PutMapping("/messages/read-batch")
    public Result<Void> markReadBatch(@RequestParam(required = false) Integer type) {
        forumMessageService.markReadBatch(forumUserSupport.requireUserId(), type);
        return Result.success();
    }

    @GetMapping("/online-users")
    public Result<List<ForumOnlineUserVO>> onlineUsers() {
        List<ForumOnlineUserVO> list = new ArrayList<>();
        for (Long userId : wsSessionManager.onlineUserIds()) {
            Student student = studentMapper.selectById(userId);
            if (student == null) {
                continue;
            }
            ForumOnlineUserVO vo = new ForumOnlineUserVO();
            vo.setUserId(userId);
            vo.setNickname(student.getNickname() != null ? student.getNickname() : student.getName());
            vo.setAvatarUrl(student.getAvatarUrl());
            list.add(vo);
        }
        return Result.success(list);
    }
}
