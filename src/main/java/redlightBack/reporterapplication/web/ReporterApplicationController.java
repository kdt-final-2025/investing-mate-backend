package redlightBack.reporterapplication.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.service.ReporterApplicationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reporter-applications")
public class ReporterApplicationController {

    private final ReporterApplicationService service;

    // 사용자 → 신청/재신청 (반려 후 재신청도 이걸로)
    @PostMapping
    public ApplicationResponseDto apply(@LoginMemberId String userId) {
        return service.apply(userId);
    }

    // 사용자 → 본인 최신 신청 조회
    @GetMapping("/me")
    public ApplicationResponseDto getMyApplication(@LoginMemberId String userId) {
        return service.getMyApplication(userId);
    }
}
