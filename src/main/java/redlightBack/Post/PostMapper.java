package redlightBack.Post;

import org.springframework.stereotype.Component;
import redlightBack.Post.Dto.PostsLikedResponse;
import redlightBack.Post.Dto.PostLikeDto;
import redlightBack.Post.Dto.PostResponse;

import java.util.List;

@Component
public class PostMapper {

    public PostResponse toPostResponse (Post post){
        return new PostResponse(post.getBoardId(),
                post.getId(),
                post.getPostTitle(),
                post.getUserId(),
                post.getViewCount(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getLikeCount(),
                post.getCommentCount());
    }

    public List<PostsLikedResponse> toListPostLikeResponse (List<PostLikeDto> posts){
        return posts.stream()
                .map(post -> new PostsLikedResponse(post.boardId(),
                        post.boardName(),
                        post.postTitle(),
                        post.userId(),
                        post.viewCount(),
                        post.commentCount(),
                        (int) post.likeCount(),
                        post.createdAt())
                ).toList();
    }
}
