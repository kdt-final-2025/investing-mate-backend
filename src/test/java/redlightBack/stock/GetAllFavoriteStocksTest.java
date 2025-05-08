package redlightBack.stock;

import org.junit.jupiter.api.DisplayName;
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
class GetAllFavoriteStocksTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private FavoriteStockRepository favoriteStockRepository;

    // StockQueryRepository 는 이 메서드에선 사용되지 않으므로 Mockito.mock() 도 가능
    @Mock
    private StockQueryRepository stockQueryRepository;

    @InjectMocks
    private StockService stockService;

    private final String USER_ID = "user123";

    @Test
    @DisplayName("createFavoriteStock: 정상 저장 호출")
    void createFavoriteStock_success() {
        // given
        Long stockId = 42L;
        Stock mockStock = new Stock(/* 생성자 인자 채워주세요 */);
        when(stockRepository.findById(stockId))
                .thenReturn(Optional.of(mockStock));

        FavoriteStockRequest request = new FavoriteStockRequest(stockId);

        // when
        stockService.createFavoriteStock(USER_ID, request);

        // then
        // favoriteStockRepository.save() 가 정확한 엔티티로 호출됐는지 검증
        ArgumentCaptor<FavoriteStock> captor = ArgumentCaptor.forClass(FavoriteStock.class);
        verify(favoriteStockRepository, times(1)).save(captor.capture());

        FavoriteStock saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getStock()).isSameAs(mockStock);
    }

    @Test
    @DisplayName("createFavoriteStock: 주식 미존재 시 NoSuchElementException 발생")
    void createFavoriteStock_stockNotFound() {
        // given
        Long stockId = 99L;
        when(stockRepository.findById(stockId))
                .thenReturn(Optional.empty());

        FavoriteStockRequest request = new FavoriteStockRequest(stockId);

        // when / then
        assertThatThrownBy(() ->
                stockService.createFavoriteStock(USER_ID, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("해당하는 주식이 없습니다.");

        // save() 호출되지 않아야 함
        verifyNoInteractions(favoriteStockRepository);
    }
}
