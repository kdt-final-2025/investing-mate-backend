package redlightBack.member;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.member.memberDto.MemberMapper;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;
import redlightBack.member.memberEntity.Member;

import java.util.Map;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 1) Jwt → MemberRequestDto 변환
    // 2) Save 로직 (기존 provisionUser) 재활용

    @Transactional
    public MemberResponseDto provisionUserFromJwt(Jwt jwt) {
        // 1) user_metadata 클레임 자체가 없으면 빈 맵으로 대체
        @SuppressWarnings("unchecked")
        Map<String, Object> rawMeta = Optional.ofNullable(jwt.getClaim("user_metadata"))
                .filter(m -> m instanceof Map)
                .map(m -> (Map<String, Object>) m)
                .orElseGet(Map::of);

        // 2) full_name → name → "Unknown" 순으로 안전하게 꺼내기
        String fullName = Optional.ofNullable(rawMeta.get("full_name"))
                .or(() -> Optional.ofNullable(rawMeta.get("name")))
                .map(Object::toString)
                .orElse("Unknown");

        // 3) 이메일도 toString() 으로 안전하게 추출 (또는 getClaimAsString)
        String email = Optional.ofNullable(jwt.getClaim("email"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("JWT 에 email 클레임이 없습니다"));

        MemberRequestDto dto = new MemberRequestDto(
                jwt.getSubject(),  // sub
                email,
                fullName
        );
        return provisionUser(dto);
    }

    // 실제 Save 로직
    @Transactional
    public MemberResponseDto provisionUser(MemberRequestDto memberRequestDto) {
        // ① userId 로 존재 여부 확인
        Member member = memberRepository.findByUserId(memberRequestDto.userId())
                // ② 없으면 새 엔티티 생성
                .orElseGet(() -> MemberMapper.toEntity(memberRequestDto));

        // 이메일·이름 최신화
        member.updateProfile(memberRequestDto.email(), memberRequestDto.fullname());

        // insert or update
        Member saved = memberRepository.save(member);
        return MemberMapper.toResponseDto(saved);
    }

    // 주어진 userId가 관리자 권한인지 조회
    @Transactional(readOnly = true)
    public boolean isAdministrator(String userId) {
        return memberRepository.findByUserId(userId)
                .map(member -> member.isAdministrator())
                .orElse(false);
    }
}
