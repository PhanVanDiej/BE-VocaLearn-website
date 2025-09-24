package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.config.MinIOProperties;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.exception.InvalidImageException;

import jakarta.transaction.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class MinIO_MediaService {
    @Autowired
    private S3Client s3Client;
    @Autowired
    private MinIOProperties minIOProperties;
    @Autowired
    private S3Presigner s3Presigner;

    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/octet-stream");

    public String uploadFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            String fileExtension = getFileExtension(file.getOriginalFilename());

            contentType = normalizeContentType(contentType, fileExtension);

            // Kiểm tra content-type sau khi đã xử lý
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidImageException("Invalid image's content type: " + contentType);
            }
            // Create unique file name
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

            System.out.println("Uploading to bucket: " + minIOProperties.getBucket());
            System.out.println("Key: " + uniqueFileName);
            System.out.println("Content-Type: " + contentType);

            // Upload new file
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(minIOProperties.getBucket())
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .build();
            try {
                s3Client.putObject(request,
                        RequestBody.fromBytes(file.getBytes()));
            } catch (S3Exception e) {
                System.err.println("❌ S3Exception:");
                System.err.println("  StatusCode: " + e.statusCode());
                System.err.println("  ErrorCode: " + e.awsErrorDetails().errorCode());
                System.err.println("  Message: " + e.awsErrorDetails().errorMessage());
                throw e; // hoặc throw custom
            }

            return uniqueFileName;
        }
        return null;
    }

    public String uploadFile(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
            throw new InvalidImageException("Image file is empty or not found");
        }

        final String originalFilename = imageFile.getName();
        final String ext = getFileExtension(originalFilename);

        String contentType = Files.probeContentType(imageFile.toPath()); // OS/MIME map

        contentType = normalizeContentType(contentType, ext); // xử lý lại nếu là octet-stream, null

        if (contentType == null || !(contentType.startsWith("image/") ||
                contentType.startsWith("audio/")))
            throw new InvalidImageException("Invalid image's content type: " + contentType);

        String uniqueFileName = generateUniqueFileName(originalFilename);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(req, RequestBody.fromFile(imageFile));
        } catch (S3Exception e) {
            System.err.println("❌ S3Exception:");
            System.err.println("  StatusCode: " + e.statusCode());
            System.err.println("  ErrorCode: " + e.awsErrorDetails().errorCode());
            System.err.println("  Message: " + e.awsErrorDetails().errorMessage());
            throw e;
        }

        return uniqueFileName;
    }

    private String normalizeContentType(String contentType, String extension) {
        if (contentType != null && !contentType.equals("application/octet-stream")) {
            return contentType;
        }
        if (extension == null)
            return null;

        switch (extension) {
            case "jpg":
                return "image/jpg";
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            // Nếu muốn hỗ trợ audio:
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "m4a":
                return "audio/mp4";
            default:
                return null;
        }
    }

    // Create presigned URL for file Media with duration
    public String getPresignedURL(String fileName, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(fileName)
                .build();
        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(duration)          // ví dụ Duration.ofSeconds(60)
                .build();

        PresignedGetObjectRequest result = s3Presigner.presignGetObject(presign);
        return result.url().toExternalForm();
    }

    // Generate unique file name
    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    // Delete file with file name
    public void deleteFile(String fileName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(fileName)
                .build();

        s3Client.deleteObject(request);
    }

    public void copyFile(String sourceKey) {
        String copySource = minIOProperties.getBucket() + "/" + sourceKey;
        String destinationFileName = generateUniqueFileName(sourceKey);
        CopyObjectRequest request = CopyObjectRequest.builder()
                .copySource(copySource)
                .destinationBucket(minIOProperties.getBucket())
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

    // private boolean isValidImageExtension(String extension) {
    //     if (extension == null || extension.isEmpty()) {
    //         return false;
    //     }
    //     // Danh sách phần mở rộng hợp lệ
    //     List<String> validExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    //     return validExtensions.contains(extension);
    // }

    // private String guessAudioContentType(String extension) {
    //     return switch (extension) {
    //         case "mp3" -> "audio/mpeg";
    //         case "wav" -> "audio/wav";
    //         case "ogg" -> "audio/ogg";
    //         case "m4a" -> "audio/mp4";
    //         default -> null;
    //     };
    // }

    @Transactional
    public void deleteQuestionMedia(ToeicQuestion question) {
        if (question.getImage() != null)
            deleteFile(question.getImage());
        if (question.getAudio() != null)
            deleteFile(question.getAudio());
        return;
    }
}
