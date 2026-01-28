package com.example.demo.controller;

import com.example.demo.domain.Result;
import com.example.demo.http.HttpResult;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传接口
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*") // 跨域支持（生产环境需限定具体域名）
public class UploadController {

    // 从配置文件读取上传路径
    @Value("${spring.upload.path}")
    private String uploadPath;

    /**
     * 多图片上传接口（对应前端FormData的files参数）
     */
    @PostMapping("/image")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
        // 1. 校验文件数组
        if (files == null || files.length == 0) {
            return Result.fail("请选择要上传的图片");
        }

        // 2. 定义允许的图片格式
        List<String> allowExts = List.of("jpg", "jpeg", "png", "webp");
        List<String> uploadUrls = new ArrayList<>();

        // 3. 遍历处理每个文件
        for (MultipartFile file : files) {
            try {
                // 3.1 校验文件大小（也可通过配置文件限制，此处兜底）
                long fileSize = file.getSize();
                if (fileSize > 12 * 1024 * 1024) { // 12MB
                    return Result.fail("文件" + file.getOriginalFilename() + "超过12MB，无法上传");
                }

                // 3.2 校验文件格式
                String originalFilename = file.getOriginalFilename();
                String ext = FilenameUtils.getExtension(originalFilename).toLowerCase();
                if (!allowExts.contains(ext)) {
                    return Result.fail("文件" + originalFilename + "格式错误，仅支持jpg/png/webp");
                }

                // 3.3 生成唯一文件名（避免重复）
                String uuid = UUID.randomUUID().toString().replace("-", "");
                String newFilename = uuid + "." + ext;

                // 3.4 创建上传目录（不存在则创建）
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 3.5 保存文件到本地
                File destFile = new File(uploadPath + newFilename);
                file.transferTo(destFile);

                // 3.6 拼接可访问的URL（根据实际部署路径调整）
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String fileUrl = baseUrl + "/upload/images/" + newFilename;
                uploadUrls.add(fileUrl);

            } catch (IOException e) {
                e.printStackTrace();
                return Result.fail("文件" + file.getOriginalFilename() + "上传失败：" + e.getMessage());
            }
        }

        // 4. 返回上传成功的URL列表
        return Result.success(uploadUrls);
    }

}
