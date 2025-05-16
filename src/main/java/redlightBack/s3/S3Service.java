package redlightBack.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

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


    // S3에 파일을 업로드하고, 공개 접근 가능한 URL을 반환합니다.
    public String upload(MultipartFile file) throws IOException {
        String key = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try (InputStream in = file.getInputStream()) {
            var req = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(req, RequestBody.fromInputStream(in, file.getSize()));
        }
        // 버킷이 공개 읽기로 설정되어 있다고 가정하고 절대 URL 생성
        String region = s3Client.serviceClientConfiguration().region().id();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }


    // S3에서 객체를 삭제합니다.
    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(req);
    }
}
