package redlightBack.member;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    //소셜 로그인 프로비저닝 시 이름·이메일을 업데이트합니다.
    public void updateProfile(String email, String fullname) {
        this.email = email;
        this.fullname = fullname;
    }

    // 일반 사용자에서 기자로 권한을 승격합니다.
    public void upgradeToReporter() {
        this.role = Role.REPORTER;
    }
}