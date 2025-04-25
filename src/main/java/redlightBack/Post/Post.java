package redlightBack.Post;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.common.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private String postTitle;
    private String userId;

    @Lob
    @Column(nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_image_urls", joinColumns = @JoinColumn(name = "post_id"))
    private List<String> imageUrls;

    private int viewCount;

    private int commentCount;

    private boolean likedByMe = false;

    private int likeCount;

    public Post(Long boardId, String postTitle, String userId, String content, List<String> imageUrls) {
        this.boardId = boardId;
        this.postTitle = postTitle;
        this.userId = userId;
        this.content = content;
        this.imageUrls = new ArrayList<>(imageUrls);
    }

    public void updatePost(String postTitle, String content, List<String> imageUrls) {
        this.postTitle = postTitle;
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public void limitationOfImages(List<String> imageUrls){
        if(imageUrls.size() > 5){
            throw new RuntimeException("이미지는 최대 5개까지 첨부할 수 있습니다.");
        }
    }
}
