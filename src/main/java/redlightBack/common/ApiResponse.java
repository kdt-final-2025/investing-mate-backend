package redlightBack.common;

import java.util.Objects;

// 공통 API 응답 래퍼
// @param <T> 실제 응답 데이터 타입

public record ApiResponse<T>(
        String message,
        T body
) {

    // 생성자 내부에서 message가 null인 경우 빈 문자열로 대체

    public ApiResponse {
        message = Objects.requireNonNullElse(message, "");
    }

    //에러 응답 생성
    //@param message 에러 메시지

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(message, null);
    }

    // 성공 응답 생성
    // @param body 실제 응답 데이터

    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>("", body);
    }
}