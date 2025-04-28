package redlightBack.reporterapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.reporterapplication.domain.ReporterApplication;
import redlightBack.reporterapplication.domain.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ReporterApplicationRepository
        extends JpaRepository<ReporterApplication, Long> {

    List<ReporterApplication> findByStatus(RequestStatus status);
    Optional<ReporterApplication> findByMember_UserId(String userId);
}
