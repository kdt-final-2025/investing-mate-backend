package redlightBack.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteNewsTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    private News sampleNews;
    private LocalDateTime deleteTime;

    @BeforeEach
    void setUp() {
        // 샘플 News 엔티티 준비
        sampleNews = new News(
                "제목1",
                "설명1",
                List.of("url1", "url2"),
                LocalDateTime.of(2025, 4, 24, 10, 0),
                "user-123"
        );
        deleteTime = LocalDateTime.of(2025, 4, 24, 12, 0);
    }

    @Test
    void delete_shouldSetDeletedAt_whenNewsExists() {
        // given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(sampleNews));

        // when
        newsService.delete(1L);

        // then
        verify(newsRepository, times(1)).findById(1L);
        assertThat(sampleNews.getDeletedAt()).isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void delete_shouldThrowException_whenNewsNotFound() {
        // given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> newsService.delete(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당하는 뉴스가 없습니다.");

        verify(newsRepository, times(1)).findById(999L);
        // deletedAt 는 절대 설정되지 않아야 함
        assertThat(sampleNews.getDeletedAt()).isNull();
    }
}
