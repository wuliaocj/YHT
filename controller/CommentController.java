package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.mapper.ProductCommentMapper;
import com.example.demo.service.CommentService;
import com.example.demo.vo.CommentRequestVO;
import com.example.demo.vo.PageResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProductCommentMapper productCommentMapper;

    @PostMapping("/product")
    public HttpResult addProductComment(@Validated @RequestBody CommentRequestVO request, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        commentService.addProductComment(userId, request);
        return HttpResult.ok("评论成功");
    }

    @PostMapping("/order")
    public HttpResult addOrderComment(@Validated @RequestBody CommentRequestVO request, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        commentService.addProductComment(userId, request);
        return HttpResult.ok("评论成功");
    }

    @GetMapping("/product/{productId}")
    public HttpResult getProductComments(@PathVariable Long productId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        PageResponseVO<?> comments = commentService.getProductComments(productId, page, size);
        return HttpResult.ok(comments);
    }

    @GetMapping("/user")
    public HttpResult getUserComments(HttpServletRequest httpRequest, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        PageResponseVO<?> comments = commentService.getUserComments(userId, page, size);
        return HttpResult.ok(comments);
    }

    @GetMapping("/check")
    public HttpResult checkCommentStatus(@RequestParam Integer orderId, @RequestParam(required = false) Long productId) {
        if (productId != null) {
            boolean hasCommented = commentService.hasCommented(orderId, productId);
            return HttpResult.ok(hasCommented);
        } else {
            boolean hasCommented = commentService.hasOrderCommented(orderId);
            return HttpResult.ok(hasCommented);
        }
    }

    @GetMapping("/count/{productId}")
    public HttpResult getProductCommentCount(@PathVariable Long productId) {
        int count = productCommentMapper.countByProductId(productId);
        return HttpResult.ok(count);
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public HttpResult deleteComment(@PathVariable Integer id, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        
        // 检查评论是否存在且属于当前用户
        boolean deleted = commentService.deleteComment(id, userId);
        if (deleted) {
            return HttpResult.ok("删除成功");
        } else {
            return HttpResult.error("删除失败，评论不存在或无权删除");
        }
    }
}