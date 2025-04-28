package redlightBack.Post.Dto;

public record PageInfo(int pageNumber,
                       int size,
                       long totalElements,
                       int totalPages) {
}
