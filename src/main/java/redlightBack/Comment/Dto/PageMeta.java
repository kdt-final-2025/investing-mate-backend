package redlightBack.Comment.Dto;

public record PageMeta(
        int totalPage,
        Long totalCount,
        int pageNumber,
        int pageSize
) {}