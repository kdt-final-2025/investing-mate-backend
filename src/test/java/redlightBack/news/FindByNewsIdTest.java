package redlightBack.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import redlightBack.news.dto.NewsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindByNewsIdTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    private News sampleNews;

    @BeforeEach
    void setUp() {
        // 공통으로 사용할 샘플 엔티티 준비
        sampleNews = new News(
                "제목1",
                "설명1",
                List.of("url1", "url2"),
                LocalDateTime.of(2025, 4, 24, 10, 0),
                "user-123"
        );
        ReflectionTestUtils.setField(sampleNews, "id", 100L);
        LocalDateTime now = LocalDateTime.of(2025, 4, 24, 11, 0);
        ReflectionTestUtils.setField(sampleNews, "createdAt", now);
        ReflectionTestUtils.setField(sampleNews, "updatedAt", now);
    }

    @Test
    void findById_shouldReturnResponse_whenNewsExists() {
        // given
        when(newsRepository.findById(100L)).thenReturn(Optional.of(sampleNews));

        // when
        NewsResponse response = newsService.findById(100L);

        // then: repository 호출 검증
        verify(newsRepository, times(1)).findById(100L);

        // and: DTO 필드 검증
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("제목1");
        assertThat(response.description()).isEqualTo("설명1");
        assertThat(response.imageUrls()).containsExactly("url1", "url2");
        assertThat(response.publishedAt()).isEqualTo(sampleNews.getPublishedAt());
        assertThat(response.userId()).isEqualTo("user-123");
        assertThat(response.createdAt()).isEqualTo(sampleNews.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(sampleNews.getUpdatedAt());
    }

    @Test
    void findById_shouldThrowException_whenNewsNotFound() {
        // given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> newsService.findById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당하는 뉴스가 없습니다.");

        verify(newsRepository, times(1)).findById(999L);
    }
}
