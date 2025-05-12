package redlightBack.Comment;

import org.springframework.stereotype.Component;
import redlightBack.Comment.Dto.CommentSortedByLikesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LikeSortedCommentTreeBuilder {

    // CommentSortedByLikesResponse를 사용하여 트리 구조로 변환
    public List<CommentSortedByLikesResponse> buildFromResponses(List<CommentSortedByLikesResponse> flatResponses) {
        Map<Long, CommentSortedByLikesResponse> commentMap = flatResponses.stream()
                .collect(Collectors.toMap(CommentSortedByLikesResponse::getCommentId, Function.identity()));

        List<CommentSortedByLikesResponse> roots = new ArrayList<>();

        for (CommentSortedByLikesResponse response : flatResponses) {
            if (response.getParentId() == null) {
                roots.add(response); // 최상위 댓글은 roots에 추가
            } else {
                CommentSortedByLikesResponse parent = commentMap.get(response.getParentId());
                if (parent != null) {
                    parent.getChildren().add(response); // 자식 댓글을 부모 댓글에 추가
                }
            }
        }

        return roots;
    }
}
