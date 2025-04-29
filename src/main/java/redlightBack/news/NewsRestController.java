package redlightBack.news;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.news.dto.NewsRequest;
import redlightBack.news.dto.NewsResponse;
import redlightBack.news.dto.PageResponse;

@AllArgsConstructor
@RestController
public class NewsRestController {

    private final NewsService newsService;

    @PostMapping("/news")
    public NewsResponse create(@LoginMemberId String userId, @Valid @RequestBody NewsRequest request) {
        return newsService.create(userId, request);
    }

    @GetMapping("/news/{newsId}")
    public NewsResponse findById(@PathVariable Long newsId) {
        return newsService.findById(newsId);
    }

    @PutMapping("/news/{newsId}")
    public void delete(@LoginMemberId String userId, @PathVariable Long newsId) {
        newsService.delete(newsId);
    }

    @GetMapping("/news")
    public PageResponse getAll(@RequestParam(required = false) String title,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam String sortBy,
                               @RequestParam String order) {
        return newsService.getAll(title, page, size, sortBy, order);
    }
}
