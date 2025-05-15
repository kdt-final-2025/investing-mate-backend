package redlightBack.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public String uploadImage(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(req, RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            log.error("S3 업로드 실패: bucket={}, key={}", bucket, key, e);
            throw e;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region(),
                key);
    }

    public void deleteImage(String key) {
        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
        } catch (S3Exception e) {
            log.error("S3 삭제 실패: bucket={}, key={}", bucket, key, e);
            throw e;
        }
    }
}
