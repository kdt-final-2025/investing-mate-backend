package redlightBack.news.dto;

import java.util.List;

public record NewsPageResponse(
        int totalPage,
        int currentPage,
        int pageSize,
        List<NewsResponse> responses
) {
}
