package redlightBack.Comment.Dto;

import java.util.List;

public record CommentResponseAndPaging(
                                       List<CommentResponse> items,
                                       PageMeta pageMeta
) {
//    public record CommentItem(
//            Long commentId,
//            String userId,
//            String content,
//            int likeCount,
//            boolean likedByMe,
//            LocalDateTime createdAt,
//            List<CommentResponse.CommentItem.ReplyItem> replies // 대댓글
//    ) {
//        public record ReplyItem(
//                Long commentId,
//                String userId,
//                String content,
//                int likeCount,
//                boolean likedByMe,
//                LocalDateTime createdAt
//        ) {}
//    }
//
//    public record PageMeta(
//            int totalPage,
//            int totalCount,
//            int pageNumber,
//            int pageSize
//    ) {}
}
