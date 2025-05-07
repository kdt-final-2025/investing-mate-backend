package redlightBack.Comment.Dto;

import java.util.List;

public record CommentResponseAndPaging(
        List<CommentResponse> items,
        PageMeta pageMeta
) {}