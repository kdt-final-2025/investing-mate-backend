package redlightBack.Comment.Dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentResponseAndPaging {
    private final List<CommentResponse> items;
    private final PageMeta pageMeta;

    public CommentResponseAndPaging(List<CommentResponse> items, PageMeta pageMeta) {
        this.items = items;
        this.pageMeta = pageMeta;
    }
}
