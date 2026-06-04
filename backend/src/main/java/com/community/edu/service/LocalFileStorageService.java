package com.community.edu.service;

import com.community.edu.common.exception.BizException;
import com.community.edu.common.exception.ErrorCode;
import com.community.edu.student.dto.StudentP1Rows;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件存储服务。处理文件上传、下载及删除。
 */
@Service
public class LocalFileStorageService {

    private final Path rootPath;

    public LocalFileStorageService(@Value("${app.file.local-root:uploads}") String root) {
        this.rootPath = Paths.get(root).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file, Long campusId) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }
        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
            ? Path.of(file.getOriginalFilename()).getFileName().toString()
            : "upload.bin";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String objectKey = campusId + "/" + UUID.randomUUID() + extension;
        Path target = rootPath.resolve(objectKey).normalize();
        if (!target.startsWith(rootPath)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件路径不合法");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件保存失败");
        }
        return new StoredFile(
            objectKey,
            originalFilename,
            StringUtils.hasText(file.getContentType()) ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
            file.getSize()
        );
    }

    public ResponseEntity<Resource> response(StudentP1Rows.FileRow row, boolean attachment) {
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        if (!"LOCAL".equalsIgnoreCase(row.getStorageType())) {
            if (StringUtils.hasText(row.getUrl())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(row.getUrl())).body(null);
            }
            throw new BizException(ErrorCode.NOT_FOUND, "文件地址不存在");
        }
        Path filePath = rootPath.resolve(row.getObjectKey()).normalize();
        if (!filePath.startsWith(rootPath) || !Files.exists(filePath)) {
            throw new BizException(ErrorCode.NOT_FOUND, "本地文件不存在");
        }
        FileSystemResource resource = new FileSystemResource(filePath);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(row.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(row.getContentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        ContentDisposition disposition = attachment
            ? ContentDisposition.attachment().filename(row.getFileName()).build()
            : ContentDisposition.inline().filename(row.getFileName()).build();
        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(resource);
    }

    public record StoredFile(String objectKey, String fileName, String contentType, long fileSize) {
    }
}
