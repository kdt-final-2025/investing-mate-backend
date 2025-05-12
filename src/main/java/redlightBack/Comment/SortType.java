package redlightBack.Comment;

public enum SortType {
    LIKE,
    TIME;

    public static SortType from(String value) {
        try {
            return SortType.valueOf(value.toUpperCase());  // 대소문자 구분 없이 처리
        } catch (IllegalArgumentException e) {
            return TIME; // 기본값 처리
        }
    }
}