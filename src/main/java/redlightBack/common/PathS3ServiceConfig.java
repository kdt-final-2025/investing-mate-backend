/* src/main/java/redlightBack/common/S3Service.java */
package redlightBack.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.nio.file.Path;

@Service
public class PathS3ServiceConfig {
    private final S3Client s3Client;
    private final String bucket;

    public PathS3ServiceConfig(S3Client s3Client, @Value("${spring.cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public void uploadFile(String key, Path filePath) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.putObject(req, RequestBody.fromFile(filePath.toFile()));
    }
}
