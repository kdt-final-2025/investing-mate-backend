package redlightBack.Comment.Dto;

import java.util.List;

public class CommentResponseAndPaging {
    private List<CommentResponse> items;
    private PageMeta pageMeta;

    public CommentResponseAndPaging(List<CommentResponse> items, PageMeta pageMeta) {
        this.items = items;
        this.pageMeta = pageMeta;
    }

    public List<CommentResponse> getItems() {
        return items;
    }

    public PageMeta getPageMeta() {
        return pageMeta;
    }
}
