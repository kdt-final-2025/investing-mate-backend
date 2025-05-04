package redlightBack.reporterapplication.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.service.ReporterApplicationService;
import redlightBack.reporterapplication.web.dto.ProcessRequestDto;

import java.util.List;

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

    // 관리자 → 다중 상태 조회
    @GetMapping
    public List<ApplicationResponseDto> listByStatuses(
            @LoginMemberId String userId,
            @RequestParam(defaultValue = "PENDING,REJECTED") List<RequestStatus> statuses
    ) {
        return service.listByStatuses(userId, statuses);
    }

    // 관리자 → 다중 승인/반려 처리
    @PatchMapping
    public List<ApplicationResponseDto> process(
            @LoginMemberId String userId,
            @RequestBody ProcessRequestDto dto
    ) {
        return service.process(userId, dto.ids(), dto.action());
    }
}
