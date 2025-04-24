package redlightBack.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import redlightBack.news.dto.NewsRequest;
import redlightBack.news.dto.NewsResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class NewsTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    private NewsRequest request;
    private String userId;
    private LocalDateTime publishedAt;

    @BeforeEach
    void setUp() {
        publishedAt = LocalDateTime.of(2025, 4, 24, 10, 0);
        request = new NewsRequest(
                "테스트 제목",
                "테스트 설명",
                List.of("http://img1.jpg", "http://img2.jpg"),
                publishedAt
        );
        userId = "user-123";
    }

    @Test
    void create_shouldSaveNewsAndReturnResponse() {
        // 준비: 저장될 엔티티 모킹
        News saved = new News(
                request.title(),
                request.description(),
                request.imageUrls(),
                request.publishedAt(),
                userId
        );
        // BaseEntity 필드 세팅
        ReflectionTestUtils.setField(saved, "id", 42L);
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(saved, "createdAt", now);
        ReflectionTestUtils.setField(saved, "updatedAt", now);

        when(newsRepository.save(any(News.class))).thenReturn(saved);

        // 실행
        NewsResponse response = newsService.create(userId, request);

        // 검증: repository.save에 전달된 News 객체 검증
        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(newsRepository, times(1)).save(captor.capture());
        News toSave = captor.getValue();

        assertThat(toSave.getTitle()).isEqualTo(request.title());
        assertThat(toSave.getDescription()).isEqualTo(request.description());
        assertThat(toSave.getImageUrls()).containsExactlyElementsOf(request.imageUrls());
        assertThat(toSave.getPublishedAt()).isEqualTo(request.publishedAt());
        assertThat(toSave.getUserId()).isEqualTo(userId);

        // 검증: 반환된 DTO 검증
        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.description()).isEqualTo(request.description());
        assertThat(response.imageUrls()).containsExactlyElementsOf(request.imageUrls());
        assertThat(response.publishedAt()).isEqualTo(request.publishedAt());
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
