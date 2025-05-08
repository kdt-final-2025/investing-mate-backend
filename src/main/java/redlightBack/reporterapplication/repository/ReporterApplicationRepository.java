package redlightBack.reporterapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.reporterapplication.domain.ReporterApplication;
import redlightBack.reporterapplication.domain.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ReporterApplicationRepository
        extends JpaRepository<ReporterApplication, Long> {

    List<ReporterApplication> findByStatusIn(List<RequestStatus> statuses);

    // 중복 신청 방지를 위한 메서드
    boolean existsByMember_UserIdAndStatusIn(String userId, List<RequestStatus> statuses);

    // 본인 최신 신청 조회
    Optional<ReporterApplication> findTopByMember_UserIdOrderByAppliedAtDesc(String userId);
}
