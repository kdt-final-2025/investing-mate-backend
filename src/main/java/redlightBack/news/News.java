package redlightBack.news;

import jakarta.persistence.*;
import lombok.Getter;
import redlightBack.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

    private LocalDateTime publishedAt;

    private String userId;

    private int viewCount = 0;

    private LocalDateTime deletedAt;

    protected News() {
    }

    public News(String title, String description, List<String> imageUrls, LocalDateTime publishedAt, String userId) {
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
        this.publishedAt = publishedAt;
        this.userId = userId;
    }

    public void deleteNews() {
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
