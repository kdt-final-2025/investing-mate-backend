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
    public ApplicationResponseDto apply(
            @LoginMemberId String userId
    ) {
        return service.apply(userId);
    }

    // 2) 사용자 → 본인 신청 상태 조회 - 신청 기록이 없으면 404 반환
    @GetMapping("/me")
    public ApplicationResponseDto getMyApplication(
            @LoginMemberId String userId
    ) {
        return service.getMyApplication(userId);
    }

    // 3) 사용자 → 본인 재신청 (반려된 경우에만 가능)
    @PutMapping("/me")
    public ApplicationResponseDto resubmit(
            @LoginMemberId String userId
    ) {
        return service.resubmit(userId);
    }

    // 4) 관리자 → 다중 대기 & 반려 목록 조회
    @GetMapping("/admin")
    public List<ApplicationResponseDto> listByStatuses(
            @LoginMemberId String userId,
            @RequestParam(defaultValue = "PENDING,REJECTED") List<RequestStatus> statuses
    ) {
        service.authorizeAdmin(userId);
        return service.listByStatuses(statuses);
    }

    // 5) 관리자 → 다중 승인/반려 처리
    @PatchMapping("/admin")
    public List<ApplicationResponseDto> process(
            @LoginMemberId String userId,
            @RequestBody ProcessRequestDto processRequestDto
    ) {
        service.authorizeAdmin(userId);
        return service.process(processRequestDto.ids(), processRequestDto.action());
    }
}
