package redlightBack.news.dto;

import java.util.List;

public record PageResponse(
        int totalPage,
        int currentPage,
        int pageSize,
        List<NewsResponse> responses
) {
}
