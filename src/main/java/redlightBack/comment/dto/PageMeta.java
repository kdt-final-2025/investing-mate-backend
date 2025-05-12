package redlightBack.comment.dto;

public record PageMeta(
        int totalPage,
        Long totalCount,
        int pageNumber,
        int pageSize
) {}