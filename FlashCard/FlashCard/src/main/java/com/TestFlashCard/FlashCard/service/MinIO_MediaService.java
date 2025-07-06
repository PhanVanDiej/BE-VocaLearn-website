package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.exception.InvalidImageException;

import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class MinIO_MediaService {
    @Autowired
    private S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucket;

    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/octet-stream");

    public void uploadFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            String fileExtension = getFileExtension(file.getOriginalFilename());

            // Xử lý trường hợp application/octet-stream dựa vào extension
            if (contentType != null && contentType.equals("application/octet-stream")
                    && isValidImageExtension(fileExtension)) {
                switch (fileExtension) {
                    case "jpg":
                        contentType = "image/jpg";
                        break;
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "gif":
                        contentType = "image/gif"; 
                        break;
                    case "webp":
                        contentType = "image/webp";
                        break;
                }
            }

            // Kiểm tra content-type sau khi đã xử lý
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidImageException("Invalid image's content type: " + contentType);
            }
            // Create unique file name
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

            // Upload new file
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getInputStream().available()));
        }
    }

    // Create presigned URL for file Media with duration
    public String getPresignedURL(String fileName, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket)
                .key(fileName)).toExternalForm();
    }

    // Generate unique file name
    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    // Delete file with file name
    public void deleteFile(String fileName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        s3Client.deleteObject(request);
    }

    public void copyFile(String sourceKey) {
        String copySource = bucket + "/" + sourceKey;
        String destinationFileName = generateUniqueFileName(sourceKey);
        CopyObjectRequest request = CopyObjectRequest.builder()
                .copySource(copySource)
                .destinationBucket(bucket)
                .destinationKey(destinationFileName)
                .build();

        s3Client.copyObject(request);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        // Danh sách phần mở rộng hợp lệ
        List<String> validExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        return validExtensions.contains(extension);
    }

    private String guessAudioContentType(String extension) {
        return switch (extension) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";
            default -> null;
        };
    }
}
