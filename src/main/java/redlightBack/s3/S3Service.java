/* src/main/java/redlightBack/s3/S3Service.java */
package redlightBack.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${spring.cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region(),
                key);
    }
}