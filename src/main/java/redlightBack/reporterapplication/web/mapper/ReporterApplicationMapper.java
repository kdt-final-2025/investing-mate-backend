package redlightBack.reporterapplication.web.mapper;


import redlightBack.reporterapplication.domain.ReporterApplication;
import redlightBack.reporterapplication.web.dto.ApplicationResponseDto;

public class ReporterApplicationMapper {

    // 엔티티 → 응답 DTO 매핑
    public static ApplicationResponseDto toDto(ReporterApplication e) {
        return new ApplicationResponseDto(
                e.getId(),
                e.getMember().getUserId(),
                e.getStatus(),
                e.getAppliedAt(),
                e.getProcessedAt()
        );
    }
}
