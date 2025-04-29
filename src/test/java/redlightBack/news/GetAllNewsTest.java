package redlightBack.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import redlightBack.news.dto.NewsResponse;
import redlightBack.news.dto.NewsPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class GetAllNewsTest {

    @Mock
    private NewsQueryRepository newsQueryRepository;

    @InjectMocks
    private NewsService newsService;

    private News sampleNews1;
    private News sampleNews2;

    @BeforeEach
    void setUp() {
        sampleNews1 = mock(News.class);
        given(sampleNews1.getId()).willReturn(1L);
        given(sampleNews1.getTitle()).willReturn("제목1");
        given(sampleNews1.getDescription()).willReturn("설명1");
        given(sampleNews1.getImageUrls()).willReturn(List.of("url1"));
        given(sampleNews1.getPublishedAt()).willReturn(LocalDateTime.of(2025,4,28,10,0));
        given(sampleNews1.getUserId()).willReturn("userA");
        given(sampleNews1.getViewCount()).willReturn(100);

        sampleNews2 = mock(News.class);
        given(sampleNews2.getId()).willReturn(2L);
        given(sampleNews2.getTitle()).willReturn("제목2");

    }

    @Test
    void getAll_WithItems_ReturnsCorrectPageResponse() {
        // given
        String titleFilter = "검색어";
        int page = 1;
        int size = 2;
        String sortBy = "publishedAt";
        String order = "desc";

        // 리포지토리 모킹: findAll → [sampleNews1, sampleNews2], totalCount → 2
        given(newsQueryRepository.findAll(eq(titleFilter), any(Pageable.class)))
                .willReturn(List.of(sampleNews1, sampleNews2));
        given(newsQueryRepository.totalCount(eq(titleFilter)))
                .willReturn(2L);

        // when
        NewsPageResponse result = newsService.getAll(titleFilter, page, size, sortBy, order);

        // then
        assertEquals(2, result.totalPage(), "총 페이지 수가 맞아야 한다");
        assertEquals(1, result.currentPage(), "현재 페이지가 맞아야 한다");
        assertEquals(2, result.pageSize(), "페이지 크기가 맞아야 한다");

        List<NewsResponse> content = result.responses();
        assertEquals(2, content.size());

        // 첫 번째 아이템 검증
        NewsResponse r1 = content.get(0);
        assertEquals(1L, r1.id());
        assertEquals("제목1", r1.title());
        assertEquals("설명1", r1.description());
        assertEquals(List.of("url1"), r1.imageUrls());
        assertEquals(LocalDateTime.of(2025,4,28,10,0), r1.publishedAt());
        assertEquals("userA", r1.userId());
        assertEquals(100, r1.viewCount());

        // 두 번째 아이템 검증
        NewsResponse r2 = content.get(1);
        assertEquals(2L, r2.id());
        assertEquals("제목2", r2.title());
        // ... 필요하다면 추가 검증
    }

    @Test
    void getAll_NoItems_ReturnsEmptyContentAndOnePage() {
        // given
        String titleFilter = null;
        int page = 1;
        int size = 5;
        String sortBy = "viewCount";
        String order = "asc";

        given(newsQueryRepository.findAll(isNull(), any(Pageable.class)))
                .willReturn(List.of());
        given(newsQueryRepository.totalCount(isNull()))
                .willReturn(0L);

        // when
        NewsPageResponse result = newsService.getAll(titleFilter, page, size, sortBy, order);

        // then
        assertEquals(1, result.totalPage(), "0건일 때는 페이지 수 1");
        assertEquals(1, result.currentPage());
        assertEquals(5, result.pageSize());
        assertTrue(result.responses().isEmpty(), "응답 리스트는 비어 있어야 한다");
    }
}
