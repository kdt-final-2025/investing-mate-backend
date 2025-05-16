// src/main/java/redlightBack/s3/S3TestController.java
package redlightBack.s3;

import io.awspring.cloud.s3.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Slf4j
public class S3TestController {
    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.upload(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("imageUrl", imageUrl));
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "File upload failed"));
        }
    }

    // 새로 추가된 삭제 엔드포인트
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Void> deleteFile(@PathVariable String key) {
        try {
            s3Service.delete(key);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (S3Exception e) {
            log.error("파일 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
