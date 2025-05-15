package redlightBack.post.dto;

public record PageInfo(int pageNumber,
                       int size,
                       long totalElements,
                       int totalPages) {
}
