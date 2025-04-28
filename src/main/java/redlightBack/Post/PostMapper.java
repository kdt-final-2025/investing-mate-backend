package redlightBack.Post;

import org.springframework.stereotype.Component;
import redlightBack.Post.Dto.PostResponse;

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
                post.isLikedByMe(),
                post.getCommentCount());
    }
}
