package redlightBack.reporterapplication.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.web.dto.ProcessRequestDto;
import redlightBack.reporterapplication.service.ReporterApplicationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reporter-applications")
public class AdminReporterApplicationController {

    private final ReporterApplicationService service;

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
