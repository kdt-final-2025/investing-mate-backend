package redlightBack.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Slf4j
public class S3TestController {

    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = s3Service.uploadImage(file);
            return ResponseEntity.ok("File uploaded successfully! imageUrl: " + url);
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed!");
        }
    }

    @DeleteMapping("/upload/{key}")
    public ResponseEntity<String> deleteFile(@PathVariable String key) {
        try {
            s3Service.deleteImage(key);
            return ResponseEntity.ok("File deleted successfully!");
        } catch (S3Exception e) {
            log.error("파일 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File deletion failed!");
        }
    }
}
