package redlightBack.post.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import redlightBack.post.Post;

@Document(indexName = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String postTitle;

    public static PostDocument fromEntity(Post p) {
        return PostDocument.builder()
                .id(p.getId())
                .postTitle(p.getPostTitle())
                .build();
    }
}