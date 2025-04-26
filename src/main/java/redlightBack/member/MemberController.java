package redlightBack.member;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;

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
        // JWT에서 바로 DTO 생성
        MemberRequestDto req = new MemberRequestDto(
                jwt.getSubject(),                   // userId
                jwt.getClaim("email"),              // email
                jwt.getClaimAsString("fullname")    // fullname
        );

        // DTO 하나만 넘기면 됨
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