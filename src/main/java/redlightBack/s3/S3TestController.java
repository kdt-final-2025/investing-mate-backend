package redlightBack.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/s3")
@CrossOrigin(origins = "http://localhost:3000")
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

    /**
     * imageUrl 파라미터로부터 객체 키를 추출해 삭제합니다.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("imageUrl") String imageUrl) {
        // URL에서 마지막 경로 부분을 키로 사용
        String key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        try {
            s3Service.delete(key);
            return ResponseEntity.noContent().build();
        } catch (S3Exception e) {
            log.error("파일 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
