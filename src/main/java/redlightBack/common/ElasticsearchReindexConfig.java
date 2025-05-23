package redlightBack.common;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import redlightBack.post.PostRepository;
import redlightBack.post.document.PostDocument;
import redlightBack.post.repository.PostDocumentRepository;

@Configuration
public class ElasticsearchReindexConfig {
    @Bean
    public ApplicationRunner reindexOnStartup(
            PostRepository postRepository,
            PostDocumentRepository documentRepository,
            ElasticsearchOperations esOps
    ) {
        return args -> {
            IndexOperations indexOps = esOps.indexOps(PostDocument.class);

            // 인덱스가 없을 경우에만 새로 생성
            if (!indexOps.exists()) {
                indexOps.createWithMapping();
            } else {
                // 존재할 땐 매핑(mapping)만 업데이트
                indexOps.putMapping(indexOps.createMapping());
            }

            // DB → ES 재색인
            postRepository.findAll().stream()
                    .map(PostDocument::fromEntity)
                    .forEach(documentRepository::save);

            indexOps.refresh();
        };
    }
}
