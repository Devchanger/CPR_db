package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.UUID;

/**
 * Handles all file-upload IO that previously lived in UploadController / ProfileController.
 * Mitigates path traversal (P0-5): the original filename is sanitised so only the final
 * segment's extension is kept, and the stored name is a UUID (no client-controlled path).
 * Validates both extension and MIME type whitelists (P2-9).
 */
@Service
public class UploadService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov");
    private static final Set<String> IMAGE_MIME = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_MIME = Set.of("video/mp4", "video/webm", "video/quicktime");
    private static final long MAX_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024;

    @Value("${cpr.upload.dir:/opt/cpr-db/uploads}")
    private String uploadDir;

    public Map<String, String> uploadImage(MultipartFile file) {
        validate(file, IMAGE_EXTENSIONS, IMAGE_MIME, MAX_IMAGE_SIZE, "image");
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String storedName = generateStoredName(getExtension(file.getOriginalFilename()));
        Path targetDir = resolveDir("images").resolve(dateDir);
        writeFile(targetDir, storedName, file);
        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/images/" + dateDir + "/" + storedName);
        return result;
    }

    public Map<String, Object> uploadVideo(MultipartFile file) {
        validate(file, VIDEO_EXTENSIONS, VIDEO_MIME, MAX_VIDEO_SIZE, "video");
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String storedName = generateStoredName(getExtension(file.getOriginalFilename()));
        Path targetDir = resolveDir("videos").resolve(dateDir);
        writeFile(targetDir, storedName, file);
        Map<String, Object> result = new HashMap<>();
        result.put("url", "/uploads/videos/" + dateDir + "/" + storedName);
        // P2-2: do not hardcode 0. Real duration requires a media-probing library
        // (ffprobe/ffmpeg); return null when metadata is not available.
        result.put("durationSeconds", null);
        return result;
    }

    public String uploadAvatar(MultipartFile file, Long userId) {
        validate(file, IMAGE_EXTENSIONS, IMAGE_MIME, MAX_IMAGE_SIZE, "avatar");
        String storedName = userId + "_" + generateStoredName(getExtension(file.getOriginalFilename()));
        Path targetDir = resolveDir("avatars");
        writeFile(targetDir, storedName, file);
        return "/uploads/avatars/" + storedName;
    }

    private Path resolveDir(String sub) {
        String base = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        return Paths.get(base + sub);
    }

    private void writeFile(Path dir, String name, MultipartFile file) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(500, "file save failed");
        }
    }

    private void validate(MultipartFile file, Set<String> extensions, Set<String> mimeTypes,
                          long maxSize, String kind) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "uploaded file is empty");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!extensions.contains(ext)) {
            throw new BusinessException(400,
                    "unsupported " + kind + " format, allowed: " + String.join("/", extensions));
        }
        String contentType = file.getContentType();
        if (contentType == null || !mimeTypes.contains(contentType.toLowerCase())) {
            throw new BusinessException(400, "unsupported " + kind + " content type");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, kind + " size exceeds limit");
        }
    }

    // Strip path separators and ".." traversal segments; keep only the final segment's extension.
    private String getExtension(String originalName) {
        if (originalName == null) return "";
        String name = originalName.replace(java.io.File.separator, "/").replace("..", "");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        if (dot < 0) return "";
        return name.substring(dot + 1).toLowerCase();
    }

    private String generateStoredName(String ext) {
        return UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
    }
}
