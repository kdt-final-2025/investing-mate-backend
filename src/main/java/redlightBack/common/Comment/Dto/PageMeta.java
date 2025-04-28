package redlightBack.common.Comment.Dto;

public record PageMeta(
        int totalPage,
        Long totalCount,
        int pageNumber,
        int pageSize
) {}