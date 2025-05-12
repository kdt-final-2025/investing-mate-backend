package redlightBack.comment.dto;

import java.util.List;

public record CommentResponseAndPaging(
        List<CommentResponse> items,
        PageMeta pageMeta
) {}