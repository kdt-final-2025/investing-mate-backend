package redlightBack.common;

import java.util.Objects;

public record ApiResponse<T>(
        String message,
        T body
) {
    public ApiResponse {
//        message = message == null ? "" : message;
        message = Objects.requireNonNullElse(message, "");
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(message, null);
    }

    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>("", body);
    }
}
