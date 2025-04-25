package redlightBack.Post.Dto;

import java.util.List;

public record CreatePostRequest(Long boardId,
                                String postTitle,
                                String content,
                                List<String> imageUrls) {
}
