package redlightBack.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import redlightBack.member.memberDto.MemberMapper;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;

import java.util.Map;

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
        @SuppressWarnings("unchecked")
        Map<String, String> meta = jwt.getClaim("user_metadata");
        String fullName = meta.getOrDefault("full_name",
                meta.getOrDefault("name", "Unknown"));

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
        Member member = memberRepo.findByUserId(req.userId())
                .orElseGet(() -> MemberMapper.toEntity(req));

        member.updateProfile(req.email(), req.fullname());
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
