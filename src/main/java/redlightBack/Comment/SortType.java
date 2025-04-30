package redlightBack.Comment;

public enum SortType {
    TIME, LIKE;

    public static SortType from(String input) {
        try {
            return SortType.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TIME; // 기본값
        }
    }
}


