package redlightBack.news;

import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.news.dto.NewsRequest;
import redlightBack.news.dto.NewsResponse;

@RestController
public class NewsRestController {

    private final NewsService newsService;

    public NewsRestController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping("/news")
    public NewsResponse create(@LoginMemberId String userId, @RequestBody NewsRequest request) {
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
}
