package redlightBack.post.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import redlightBack.post.document.PostDocument;

import java.util.List;

@Repository
public interface PostDocumentRepository
        extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByPostTitleContainingIgnoreCase(String postTitle);
}
