package redlightBack.comment;

import org.springframework.stereotype.Component;
import redlightBack.comment.domain.Comment;
import redlightBack.comment.dto.CommentResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommentTreeBuilder {

    public List<CommentResponse> build(List<Comment> flatComments) {
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        // 1차로 Comment → CommentResponse로 변환하며 map에 담기
        for (Comment comment : flatComments) {
            Long parentId = comment.getParent() != null ? comment.getParent().getId() : null;
            CommentResponse response = new CommentResponse(
                    comment.getId(),
                    comment.getUserId(),
                    comment.getContent(),
                    comment.getLikeCount(),
                    false,
                    parentId,
                    comment.getCreatedAt(),
                    new ArrayList<>() // 자식 리스트 초기화
            );
            commentMap.put(comment.getId(), response);
        }

        // 2차로 부모-자식 연결
        for (Comment comment : flatComments) {
            Long commentId = comment.getId();
            CommentResponse child = commentMap.get(commentId);

            if (comment.getParent() == null) {
                roots.add(child); // 최상위 댓글
            } else {
                Long parentId = comment.getParent().getId();
                if (!commentId.equals(parentId)) {
                   CommentResponse parent = commentMap.get(parentId);
                   if(parent != null){
                       parent.children().add(child);// 자식 추가
                   }
                }
            }
        }

        return roots;
    }
}

