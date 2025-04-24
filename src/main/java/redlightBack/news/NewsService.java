package redlightBack.news;

import org.springframework.stereotype.Service;
import redlightBack.common.GlobalExceptionHandler;
import redlightBack.news.dto.NewsRequest;
import redlightBack.news.dto.NewsResponse;

import java.util.NoSuchElementException;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public NewsResponse create(String userId, NewsRequest request) {
        News news = newsRepository.save(new News(
                request.title(),
                request.description(),
                request.imageUrls(),
                request.publishedAt(),
                userId));

        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getImageUrls(),
                news.getPublishedAt(),
                news.getUserId(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }

    public NewsResponse findById(Long newsId) {
        News news = newsRepository.findById(newsId).orElseThrow(
                () -> new NoSuchElementException("해당하는 뉴스가 없습니다.")
        );
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getImageUrls(),
                news.getPublishedAt(),
                news.getUserId(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
