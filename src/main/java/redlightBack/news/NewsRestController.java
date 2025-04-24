package redlightBack.news;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
}
