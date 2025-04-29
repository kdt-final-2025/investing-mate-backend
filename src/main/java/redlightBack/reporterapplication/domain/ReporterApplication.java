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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;   // PENDING, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime processedAt;

    // 팩토리 메서드: 도메인 안에서 생성 로직 캡슐화
    public static ReporterApplication applyFor(Member member) {
        return ReporterApplication.builder()
                .member(member)
                .status(RequestStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    // 승인(관리자)
    public void approve() {
        this.status = RequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        member.upgradeToReporter();
    }

    // 반려(관리자)
    public void reject() {
        this.status = RequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
}
