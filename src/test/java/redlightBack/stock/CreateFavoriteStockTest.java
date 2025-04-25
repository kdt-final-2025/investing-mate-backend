package redlightBack.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import redlightBack.stock.dto.FavoriteStockRequest;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFavoriteStockTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private FavoriteStockRepository favoriteStockRepository;

    @InjectMocks
    private StockService stockService;

    private final String USER_ID = "user123";
    private final Long STOCK_ID = 42L;

    @BeforeEach
    void setUp() {
        // nothing needed here for now
    }

    @Test
    void createFavoriteStock_StockExists_savedSuccessfully() {
        // given
        Stock dummyStock = new Stock();              // 실제 생성자 시그니처에 맞게 수정
        when(stockRepository.findById(STOCK_ID))
                .thenReturn(Optional.of(dummyStock));

        FavoriteStockRequest request = new FavoriteStockRequest(STOCK_ID);

        // when
        stockService.createFavoriteStock(USER_ID, request);

        // then
        ArgumentCaptor<FavoriteStock> captor = ArgumentCaptor.forClass(FavoriteStock.class);
        verify(favoriteStockRepository, times(1)).save(captor.capture());

        FavoriteStock saved = captor.getValue();
        assertThat(saved.getStock()).isSameAs(dummyStock);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void createFavoriteStock_StockNotFound_throwsException() {
        // given
        when(stockRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        FavoriteStockRequest request = new FavoriteStockRequest(STOCK_ID);

        // when / then
        assertThatThrownBy(() ->
                stockService.createFavoriteStock(USER_ID, request)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당하는 주식이 없습니다.");

        verify(favoriteStockRepository, never()).save(any());
    }
}
