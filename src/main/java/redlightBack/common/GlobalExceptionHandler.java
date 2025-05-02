package redlightBack.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 롬복 사용 시 @Slf4j를 클래스에 사용하면 생략 가능
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ResponseStatus 사용
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ApiResponse<Void> handleNoSuchElementException(NoSuchElementException ex) {
        logger.error("message", ex);
        return ApiResponse.error(ex.getMessage());
    }

    // ResponseEntity 사용, Exception 두 개
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(RuntimeException ex) {
        logger.error("message", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
