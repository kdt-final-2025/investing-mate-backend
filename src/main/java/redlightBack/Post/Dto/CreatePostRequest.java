package redlightBack.Post.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(Long boardId,
                                @NotBlank(message = "제목을 입력하세요.")
                                @Size(max = 70) String postTitle,
                                @NotBlank(message = "본문을 입력하세요.")
                                String content,
                                @Size(max = 5, message = "이미지는 최대 5개까지 첨부 가능합니다.")
                                List<String> imageUrls) {
}
