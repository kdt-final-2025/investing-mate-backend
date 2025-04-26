package redlightBack.member;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;

import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 1) POST /members/me
     *    - Authorization 헤더의 JWT에서 sub/email/fullname(claim) 추출
     *    - 신규 생성 또는 업데이트(upsert) 수행
     */

    @PostMapping("/me")
    public MemberResponseDto provisionUser(@AuthenticationPrincipal Jwt jwt) {
        // supabase 기본 claim 중, 이름은 user_metadata 에 담겨 있습니다.
        @SuppressWarnings("unchecked")
        Map<String,String> meta = jwt.getClaim("user_metadata");
        String fullName = meta.getOrDefault("full_name",
                meta.getOrDefault("name", "Unknown"));
        MemberRequestDto req = new MemberRequestDto(
                jwt.getSubject(),
                jwt.getClaim("email"),
                fullName
        );
        return memberService.provisionUser(req);
    }


    /**
     * 2) PATCH /members/{userId}/promote
     *    - 기자 역할로 승격
     */
    @PatchMapping("/{userId}/promote")
    public MemberResponseDto promoteToReporter(@PathVariable String userId) {
        return memberService.promoteToReporter(userId);
    }
}