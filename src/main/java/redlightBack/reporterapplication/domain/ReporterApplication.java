package redlightBack.reporterapplication.domain;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.member.memberEntity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class ReporterApplication {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:N 관계로 변경하여, 반려 기록을 남기고 새 신청 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime processedAt;

    // 팩토리 메서드
    public static ReporterApplication applyFor(Member member) {
        return ReporterApplication.builder()
                .member(member)
                .status(RequestStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    public void approve() {
        this.status = RequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        member.upgradeToReporter();
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
}
