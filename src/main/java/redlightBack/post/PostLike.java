package redlightBack.post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    private String userId;

    @Setter
    private boolean liked;

    public PostLike(Post post, String userId) {
        this.post = post;
        this.userId = userId;
    }
}
