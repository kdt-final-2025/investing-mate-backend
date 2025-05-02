package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import redlightBack.indicator.dto.FavoriteIndicatorResponse;
import redlightBack.indicator.dto.FavoriteIndicatorsListResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetFavoriteIndicatorsAllTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private FavoriteIndicatorRepository favoriteIndicatorRepository;

    @Mock
    private IndicatorQueryRepository indicatorQueryRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    private final String userId = "user123";
    private final int page = 2;
    private final int size = 10;
    private final SortType sortType = SortType.LATEST;

    private List<FavoriteIndicator> mockFavorites;
    private long mockTotalCount;

    @BeforeEach
    void setUp() {
        // 1) totalCount 모킹
        mockTotalCount = 25L;
        given(indicatorQueryRepository.totalCount(eq(userId)))
                .willReturn(mockTotalCount);

        // 2) FavoriteIndicator 엔티티 목록 모킹
        FavoriteIndicator fav1 = Mockito.mock(FavoriteIndicator.class);
        Indicator ind1 = Mockito.mock(Indicator.class);
        given(fav1.getId()).willReturn(11L);
        given(ind1.getName()).willReturn("GDP");
        given(fav1.getIndicator()).willReturn(ind1);

        FavoriteIndicator fav2 = Mockito.mock(FavoriteIndicator.class);
        Indicator ind2 = Mockito.mock(Indicator.class);
        given(fav2.getId()).willReturn(12L);
        given(ind2.getName()).willReturn("CPI");
        given(fav2.getIndicator()).willReturn(ind2);

        mockFavorites = List.of(fav1, fav2);

        // 3) getAll 모킹 (offset = (page-1)*size = 10)
        given(indicatorQueryRepository.getAll(
                eq(userId),
                ArgumentMatchers.<OrderSpecifier<?>>any(),
                eq((long) (page - 1) * size),
                eq((long) size)
        )).willReturn(mockFavorites);
    }

    @Test
    @DisplayName("getFavoritesAll(): 올바른 DTO 반환")
    void testGetFavoritesAll() {
        // when
        FavoriteIndicatorsListResponse response =
                indicatorService.getFavoritesAll(userId, page, size, sortType);

        // then
        // 1) 컨텐츠 확인
        List<FavoriteIndicatorResponse> content = response.responses();
        assertEquals(2, content.size());
        assertEquals(11L, content.get(0).id());
        assertEquals("GDP", content.get(0).name());
        assertEquals(12L, content.get(1).id());
        assertEquals("CPI", content.get(1).name());

        // 2) 페이징 정보 확인
        // record 정의 순서: (responses, totalPage, currentPage, pageSize, totalCount)
        // record 정의 순서: (responses, currentPage, pageSize, totalPages, totalCount)
        int expectedTotalPages = (int) Math.ceil((double) mockTotalCount / size);
        assertEquals(page, response.currentPage(), "현재 페이지 검증");
        assertEquals(size, response.pageSize(), "페이지 크기 검증");
        assertEquals(expectedTotalPages, response.totalPages(), "총 페이지 수 검증");
        assertEquals(mockTotalCount, response.totalCount(), "전체 항목 수 검증");

        // 3) indicatorQueryRepository가 정확히 호출됐는지 검증
        Mockito.verify(indicatorQueryRepository).totalCount(eq(userId));
        Mockito.verify(indicatorQueryRepository).getAll(
                eq(userId),
                ArgumentMatchers.<OrderSpecifier<?>>any(),
                eq((long) (page - 1) * size),
                eq((long) size)
        );
    }
}
