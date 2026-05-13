package com.example.demo.controller;

import com.example.demo.domain.OperationLog;
import com.example.demo.domain.ProductComment;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.ProductCommentMapper;
import com.example.demo.service.CommentService;
import com.example.demo.service.LogService;
import com.example.demo.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProductCommentMapper productCommentMapper;

    @Autowired
    private LogService logService;

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);
            logService.recordOperationLog(operationLog);
        } catch (Exception e) {
            AdminCommentController.log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    @GetMapping("/list")
    public HttpResult getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (status != null) params.put("status", status);
            if (productId != null) params.put("productId", productId);
            if (rating != null) params.put("rating", rating);

            PageResponseVO<?> comments = commentService.getAdminCommentList(page, size, params);
            recordOperationLog("评论管理", "查询评论列表", 1, null);
            return HttpResult.ok(comments);
        } catch (Exception e) {
            recordOperationLog("评论管理", "查询评论列表", 0, e.getMessage());
            log.error("获取评论列表失败", e);
            return HttpResult.error("获取评论列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public HttpResult getCommentDetail(@PathVariable Integer id) {
        try {
            ProductComment comment = productCommentMapper.selectById(id);
            if (comment == null) {
                recordOperationLog("评论管理", "查询评论详情", 0, "评论不存在");
                return HttpResult.error("评论不存在");
            }
            recordOperationLog("评论管理", "查询评论详情", 1, null);
            return HttpResult.ok(comment);
        } catch (Exception e) {
            recordOperationLog("评论管理", "查询评论详情", 0, e.getMessage());
            log.error("获取评论详情失败", e);
            return HttpResult.error("获取评论详情失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public HttpResult updateCommentStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            ProductComment comment = productCommentMapper.selectById(id);
            if (comment == null) {
                recordOperationLog("评论管理", "更新评论状态", 0, "评论不存在");
                return HttpResult.error("评论不存在");
            }

            comment.setStatus(status);
            productCommentMapper.updateById(comment);

            log.info("更新评论状态成功，评论ID：{}，新状态：{}", id, status);
            recordOperationLog("评论管理", "更新评论状态", 1, null);
            return HttpResult.ok("更新成功");
        } catch (Exception e) {
            recordOperationLog("评论管理", "更新评论状态", 0, e.getMessage());
            log.error("更新评论状态失败", e);
            return HttpResult.error("更新评论状态失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public HttpResult deleteComment(@PathVariable Integer id) {
        try {
            ProductComment comment = productCommentMapper.selectById(id);
            if (comment == null) {
                recordOperationLog("评论管理", "删除评论", 0, "评论不存在");
                return HttpResult.error("评论不存在");
            }

            productCommentMapper.deleteById(id);

            log.info("删除评论成功，评论ID：{}", id);
            recordOperationLog("评论管理", "删除评论", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            recordOperationLog("评论管理", "删除评论", 0, e.getMessage());
            log.error("删除评论失败", e);
            return HttpResult.error("删除评论失败：" + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public HttpResult getCommentStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            Long totalComments = productCommentMapper.selectCount(null);
            int pendingComments = productCommentMapper.countByStatus(0);
            int approvedComments = productCommentMapper.countByStatus(1);
            int rejectedComments = productCommentMapper.countByStatus(2);

            stats.put("total", totalComments != null ? totalComments.intValue() : 0);
            stats.put("pending", pendingComments);
            stats.put("approved", approvedComments);
            stats.put("rejected", rejectedComments);

            recordOperationLog("评论管理", "查询评论统计", 1, null);
            return HttpResult.ok(stats);
        } catch (Exception e) {
            recordOperationLog("评论管理", "查询评论统计", 0, e.getMessage());
            log.error("获取评论统计失败", e);
            return HttpResult.error("获取评论统计失败：" + e.getMessage());
        }
    }
}
