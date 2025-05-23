package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redlightBack.indicator.dto.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IndicatorService {

    private final RestTemplate restTemplate;
    private final IndicatorRepository indicatorRepository;
    private final FavoriteIndicatorRepository favoriteIndicatorRepository;
    private final IndicatorQueryRepository indicatorQueryRepository;

    @Scheduled(cron = "0 0 0 1 * *")
    public void fetchAndSaveAll() {
        String url = "http://localhost:5000/indicators";

        // 1. JSON 배열을 LinkedHashMap[] 으로 받음
        ResponseEntity<List<LinkedHashMap<String, Object>>> resp =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<LinkedHashMap<String, Object>> body = resp.getBody();

            if (body.isEmpty()) return;

            // 2. 기존에 DB에 존재하는 event 이름 조회
            Set<String> existingNames = indicatorRepository
                    .findAllByNameIn(
                            body.stream()
                                    .map(entry -> (String) entry.get("event"))
                                    .collect(Collectors.toSet())
                    )
                    .stream()
                    .map(Indicator::getName)
                    .collect(Collectors.toSet());

            // 3. 새로 들어온 것만 저장
            List<Indicator> newIndicators = body.stream()
                    .filter(entry -> !existingNames.contains((String) entry.get("event")))
                    .map(entry -> {
                        Indicator indicator = new Indicator();
                        indicator.setName((String) entry.get("event"));
                        indicator.setKorName((String) entry.get("koName"));
                        indicator.setCountry((String) entry.get("country"));

                        // 날짜 변환
                        String dateStr = (String) entry.get("date");
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        indicator.setDate(dateTime);

                        // 숫자 필드
                        indicator.setActual(toDouble(entry.get("actual")));
                        indicator.setPrevious(toDouble(entry.get("previous")));
                        indicator.setEstimate(toDouble(entry.get("estimate")));

                        // Impact Enum 매핑
                        String impactStr = (String) entry.get("impact");
                        if (impactStr != null) {
                            indicator.setImpact(Impact.valueOf(impactStr.toUpperCase())); // "High" -> Impact.HIGH
                        }

                        return indicator;
                    })
                    .toList();

            if (!newIndicators.isEmpty()) {
                indicatorRepository.saveAll(newIndicators);
            }
        }
    }

    // null-safe Double 변환
    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.valueOf(val.toString());
    }

    @Transactional
    public void createFavoriteIndicator(String userId, FavoriteIndicatorRequest request) {
        Indicator indicator = indicatorRepository.findById(request.indicatorId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 경제 지표가 없습니다."));
        if (favoriteIndicatorRepository.findByUserIdAndIndicator_Id(userId, request.indicatorId()).isPresent()
        ) {
            throw new IllegalArgumentException("이미 등록된 경제지표 입니다.");
        }
        favoriteIndicatorRepository.save(new FavoriteIndicator(indicator, userId));
    }

    public FavoriteIndicatorsListResponse getFavoritesAll(String userId,
                                                          int page,
                                                          int size,
                                                          SortType sortType) {
        OrderSpecifier<?> orderSpecifier = sortType.getOrder(QFavoriteIndicator.favoriteIndicator);
        long totalCount = indicatorQueryRepository.favoriteTotalCount(userId);
        long offset = (long) (page - 1) * size;
        List<Indicator> favoriteIndicators = indicatorQueryRepository.getFavoriteAll(
                userId,
                orderSpecifier,
                offset,
                size);
        List<IndicatorResponse> indicatorResponses = favoriteIndicators.stream()
                .map(indicator -> new IndicatorResponse(
                        indicator.getId(),
                        indicator.getName(),
                        indicator.getKorName(),
                        indicator.getCountry(),
                        indicator.getDate(),
                        indicator.getActual(),
                        indicator.getPrevious(),
                        indicator.getEstimate(),
                        indicator.getImpact(),
                        null
                ))
                .toList();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new FavoriteIndicatorsListResponse(
                indicatorResponses,
                page,
                size,
                totalPages,
                totalCount);
    }

    public IndicatorListResponse getAll(String userId, int page, int size, String order) {
        Sort.Direction direction = Sort.Direction.fromString(order.toUpperCase());
        Sort sort = Sort.by(direction, "date");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        List<Indicator> indicators = indicatorQueryRepository.getAll(pageable);
        List<Long> favoriteIndicatorsId = favoriteIndicatorRepository.findIndicatorIdsByUserId(userId);
        Set<Long> favIdSet = new HashSet<>(favoriteIndicatorsId);
        List<IndicatorResponse> indicatorResponses = indicators.stream()
                .map(indicator -> new IndicatorResponse(
                        indicator.getId(),
                        indicator.getName(),
                        indicator.getKorName(),
                        indicator.getCountry(),
                        indicator.getDate(),
                        indicator.getActual(),
                        indicator.getPrevious(),
                        indicator.getEstimate(),
                        indicator.getImpact(),
                        favIdSet.contains(indicator.getId())
                ))
                .toList();
        long totalCount = indicatorQueryRepository.totalCount();
        return new IndicatorListResponse(
                indicatorResponses,
                totalCount);
    }

    public void deleteById(String userId, Long indicatorId) {
        FavoriteIndicator favoriteIndicator = favoriteIndicatorRepository.findByUserIdAndIndicator_Id(userId, indicatorId).orElseThrow(
                () -> new NoSuchElementException("해당하는 관심 경제 지표가 없습니다."));
        favoriteIndicatorRepository.delete(favoriteIndicator);
    }
}
