package redlightBack.indicator;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import redlightBack.indicator.dto.FavoriteIndicatorRequest;

import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class CreateFavoriteIndicatorTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private FavoriteIndicatorRepository favoriteIndicatorRepository;

    @InjectMocks
    private IndicatorService service;

    private final Long EXISTING_ID = 123L;
    private final String USER_ID = "user1";

    @BeforeEach
    void setUp() {
        // nothing to init
    }

    @Test
    void createFavoriteIndicator_success() {
        // given: DB에 존재하는 지표
        Indicator indicator = new Indicator("Some Name", null);
        // private Long id 필드에 EXISTING_ID 값 세팅
        ReflectionTestUtils.setField(indicator, "id", EXISTING_ID);

        when(indicatorRepository.findById(EXISTING_ID))
                .thenReturn(java.util.Optional.of(indicator));

        // when
        service.createFavoriteIndicator(USER_ID, new FavoriteIndicatorRequest(EXISTING_ID));

        // then: save에 들어간 FavoriteIndicator 값 검증
        verify(favoriteIndicatorRepository, times(1))
                .save(argThat(fav ->
                        EXISTING_ID.equals(fav.getIndicator().getId()) &&
                                USER_ID.equals(fav.getUserId())
                ));
    }

    @Test
    void createFavoriteIndicator_notFound_throws() {
        // given: DB에 없는 지표
        when(indicatorRepository.findById(EXISTING_ID))
                .thenReturn(java.util.Optional.empty());

        // when / then
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> service.createFavoriteIndicator(USER_ID, new FavoriteIndicatorRequest(EXISTING_ID))
        );
        assertEquals("해당하는 경제 지표가 없습니다.", ex.getMessage());

        // save는 절대 호출되지 않아야 함
        verify(favoriteIndicatorRepository, never()).save(any());
    }
}

