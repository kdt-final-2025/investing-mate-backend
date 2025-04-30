package redlightBack.reporterapplication.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import redlightBack.member.memberEntity.Member;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Role;
import redlightBack.reporterapplication.domain.ReporterApplication;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.repository.ReporterApplicationRepository;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.web.mapper.ReporterApplicationMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReporterApplicationService {

    private final ReporterApplicationRepository repo;
    private final MemberRepository memberRepo;

    // 1) 사용자 신청 or 재신청 → 기존 반려 기록은 보존
    @Transactional
    public ApplicationResponseDto apply(String userId) {
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));

        // PENDING 또는 APPROVED 중복 방지
        if (repo.existsByMember_UserIdAndStatusIn(
                userId,
                List.of(RequestStatus.PENDING, RequestStatus.APPROVED))
        ) {
            throw new IllegalStateException("이미 대기 중이거나 승인된 신청이 존재합니다");
        }

        ReporterApplication app = ReporterApplication.applyFor(member);
        return ReporterApplicationMapper.toDto(repo.save(app));
    }

    // 2) 사용자 → 본인 최신 신청 조회
    @Transactional(readOnly = true)
    public ApplicationResponseDto getMyApplication(String userId) {
        ReporterApplication app = repo.findTopByMember_UserIdOrderByAppliedAtDesc(userId)
                .orElseThrow(() -> new NoSuchElementException("신청 내역이 없습니다."));
        return ReporterApplicationMapper.toDto(app);
    }

    // 3) 관리자 전용: 상태별 조회 + 권한 검증
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> listByStatuses(String userId, List<RequestStatus> statuses) {
        authorizeAdmin(userId);
        return repo.findByStatusIn(statuses).stream()
                .map(ReporterApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    // 4) 관리자 전용: 다중 승인/반려 처리 + 권한 검증
    @Transactional
    public List<ApplicationResponseDto> process(
            String userId,
            List<Long> ids,
            RequestStatus action
    ) {
        authorizeAdmin(userId);
        return ids.stream()
                .map(id -> {
                    ReporterApplication app = repo.findById(id)
                            .orElseThrow(() -> new NoSuchElementException("신청 내역이 없습니다: " + id));
                    if (action == RequestStatus.APPROVED) {
                        app.approve();
                    } else if (action == RequestStatus.REJECTED) {
                        app.reject();
                    }
                    return ReporterApplicationMapper.toDto(app);
                })
                .collect(Collectors.toList());
    }

    // 관리자 권한 확인
    private void authorizeAdmin(String userId) {
        Member m = memberRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        if (m.getRole() != Role.ADMINISTRATOR) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }
}
