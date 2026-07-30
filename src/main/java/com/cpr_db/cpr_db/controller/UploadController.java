package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.common.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L;
    private static final Path IMAGE_DIR = Paths.get("/opt/cpr-db/uploads/images");

    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov");
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024L;
    private static final Path VIDEO_DIR = Paths.get("/opt/cpr-db/uploads/videos");

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) {
        String ext = getExtension(file);
        if (!IMAGE_EXTENSIONS.contains(ext)) {
            throw new BusinessException(400, "unsupported image format, only jpg/jpeg/png/webp allowed");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "image size cannot exceed 2MB");
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetDir = IMAGE_DIR.resolve(dateDir);
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(filename), file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(500, "image save failed");
        }
        String url = "/uploads/images/" + dateDir + "/" + filename;
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ResponseEntity.ok(ApiResponse.success(result, "uploaded"));
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadVideo(@RequestParam("file") MultipartFile file) {
        String ext = getExtension(file);
        if (!VIDEO_EXTENSIONS.contains(ext)) {
            throw new BusinessException(400, "unsupported video format, only mp4/webm/mov allowed");
        }
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException(400, "video size cannot exceed 500MB");
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetDir = VIDEO_DIR.resolve(dateDir);
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(filename), file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(500, "video save failed");
        }
        String url = "/uploads/videos/" + dateDir + "/" + filename;
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("durationSeconds", 0);
        return ResponseEntity.ok(ApiResponse.success(result, "uploaded"));
    }

    private String getExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
    }
}
