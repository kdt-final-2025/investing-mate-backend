package redlightBack.reporterapplication.web.dto;

import redlightBack.reporterapplication.domain.RequestStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDto(
        Long applicationId,
        String userId,
        String fullname,
        RequestStatus status,
        LocalDateTime appliedAt,
        LocalDateTime processedAt
) {
}
