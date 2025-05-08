package redlightBack.news;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.news.dto.NewsRequest;
import redlightBack.news.dto.NewsResponse;
import redlightBack.news.dto.NewsPageResponse;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsQueryRepository newsQueryRepository;

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
                news.getViewCount(),
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
                news.getViewCount(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }

    @Transactional
    public void delete(Long newsId) {
        News news = newsRepository.findById(newsId).orElseThrow(
                () -> new NoSuchElementException("해당하는 뉴스가 없습니다."));
        news.deleteNews();
    }

    public NewsPageResponse getAll(String title, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(sortBy, order);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        List<News> news = newsQueryRepository.findAll(title, pageable);
        long totalCount = newsQueryRepository.totalCount(title);
        int totalPage = (int) Math.ceil((double) (totalCount - 1) / pageable.getPageSize()) + 1;
        return new NewsPageResponse(
                totalPage,
                page,
                size,
                news.stream()
                        .map(n -> new NewsResponse(
                                n.getId(),
                                n.getTitle(),
                                n.getDescription(),
                                n.getImageUrls(),
                                n.getPublishedAt(),
                                n.getUserId(),
                                n.getViewCount(),
                                n.getCreatedAt(),
                                n.getUpdatedAt()
                        ))
                        .toList()
        );
    }
}