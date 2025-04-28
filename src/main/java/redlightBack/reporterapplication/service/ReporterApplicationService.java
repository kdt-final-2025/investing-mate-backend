package redlightBack.reporterapplication.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Member;
import redlightBack.member.memberEntity.Role;
import redlightBack.reporterapplication.domain.ReporterApplication;
import redlightBack.reporterapplication.domain.RequestStatus;
import redlightBack.reporterapplication.repository.ReporterApplicationRepository;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;
import redlightBack.reporterapplication.web.dto.ProcessRequestDto;
import redlightBack.reporterapplication.web.mapper.ReporterApplicationMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporterApplicationService {

    private final ReporterApplicationRepository reporterApplicationRepository;
    private final MemberRepository memberRepository             ;

    public ReporterApplicationService(
            ReporterApplicationRepository reporterApplicationRepository,
            MemberRepository memberRepository
    ) {
        this.reporterApplicationRepository = reporterApplicationRepository;
        this.memberRepository = memberRepository;
    }

    // 로그인된 회원으로 “기자 신청” 생성
    @Transactional
    public ApplicationResponseDto apply(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));

        if (reporterApplicationRepository.findByMember_UserId(userId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 신청된 사용자입니다");
        }

        ReporterApplication app = ReporterApplication.applyFor(member);
        ReporterApplication saved = reporterApplicationRepository.save(app);
        return ReporterApplicationMapper.toDto(saved);
    }

    // 관리자 전용: 상태별 조회
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> listByStatus(RequestStatus status) {
        return reporterApplicationRepository.findByStatus(status).stream()
                .map(ReporterApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    // 관리자 전용: 승인 또는 반려 처리
    @Transactional
    public ApplicationResponseDto process(Long id, ProcessRequestDto dto) {
        ReporterApplication app = reporterApplicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "신청 내역이 없습니다: " + id));

        if ("APPROVE".equalsIgnoreCase(dto.action())) {
            app.approve();
        } else {
            app.reject();
        }
        // dirty checking → 자동 저장
        return ReporterApplicationMapper.toDto(app);
    }

    // 관리자 권한 확인
    public void authorizeAdmin(String userId) {
        Member m = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
        if (m.getRole() != Role.ADMINISTRATOR) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }
}
