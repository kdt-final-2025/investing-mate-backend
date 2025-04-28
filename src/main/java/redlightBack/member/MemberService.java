package redlightBack.member;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.member.memberDto.MemberMapper;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;

import java.util.Map;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepo;

    public MemberService(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    // 1) Jwt → MemberRequestDto 변환
    // 2) Save 로직 (기존 provisionUser) 재활용

    @Transactional
    public MemberResponseDto provisionUserFromJwt(Jwt jwt) {
        // 1) user_metadata 전체를 Map<String,Object> 로 받고
        @SuppressWarnings("unchecked")
        Map<String, Object> rawMeta = (Map<String, Object>) jwt.getClaim("user_metadata");

        // 2) 필요한 키만 toString() 으로 안전하게 변환
        String fullName = Optional.ofNullable(rawMeta.get("full_name"))
                .orElse(rawMeta.get("name"))
                .toString();

        MemberRequestDto dto = new MemberRequestDto(
                jwt.getSubject(),
                jwt.getClaim("email"),
                fullName
        );
        return provisionUser(dto);
    }

    // 실제 Save 로직
    @Transactional
    public MemberResponseDto provisionUser(MemberRequestDto req) {
        // ① userId 로 존재 여부 확인
        Member member = memberRepo.findByUserId(req.userId())
                // ② 없으면 새 엔티티 생성
                .orElseGet(() -> MemberMapper.toEntity(req));

        // 이메일·이름 최신화
        member.updateProfile(req.email(), req.fullname());

        // insert or update
        Member saved = memberRepo.save(member);
        return MemberMapper.toResponseDto(saved);
    }

    //일반유저를 기자로 바꿔주는 로직
    @Transactional
    public MemberResponseDto promoteToReporter(String userId) {
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        member.upgradeToReporter();
        return MemberMapper.toResponseDto(member);
    }
}
