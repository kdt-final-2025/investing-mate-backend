package redlightBack.reporterapplication.web.dto;

import redlightBack.reporterapplication.domain.RequestStatus;

import java.util.List;

public record ProcessRequestDto(
        List<Long> ids,
        RequestStatus action // APPROVED 또는 REJECTED
) {
}
