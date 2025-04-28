package redlightBack.reporterapplication.web;

import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.service.ReporterApplicationService;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.web.dto.ProcessRequestDto;

import java.util.List;

@RestController
@RequestMapping("/reporter-applications")
public class ReporterApplicationController {

    private final ReporterApplicationService service;

    public ReporterApplicationController(ReporterApplicationService service) {
        this.service = service;
    }

    // 1) 사용자 → 기자 신청
    @PostMapping
    public ApplicationResponseDto apply(@LoginMemberId String userId) {
        return service.apply(userId);
    }

    // 2) 관리자 → 대기 목록 조회
    @GetMapping("/admin")
    public List<ApplicationResponseDto> listByStatus(
            @LoginMemberId String userId,
            @RequestParam(defaultValue = "PENDING") RequestStatus status
    ) {
        service.authorizeAdmin(userId);
        return service.listByStatus(status);
    }

    // 3) 관리자 → 승인/반려 처리
    @PatchMapping("/admin/{id}")
    public ApplicationResponseDto process(
            @LoginMemberId String userId,
            @PathVariable Long id,
            @RequestBody ProcessRequestDto dto
    ) {
        service.authorizeAdmin(userId);
        return service.process(id, dto);
    }
}
