package redlightBack.member;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.member.memberDto.RoleResponseDto;
import redlightBack.member.memberDto.MemberResponseDto;

@RestController
@RequestMapping("/member/me")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // POST /members/me
    // - Jwt를 서비스에 넘겨서 upsert 수행

    @PostMapping
    public MemberResponseDto provisionUser(@AuthenticationPrincipal Jwt jwt) {
        return memberService.provisionUserFromJwt(jwt);
    }

    // 2) GET /member/me/role
    @GetMapping("role")
    public RoleResponseDto getMyRole(@LoginMemberId String userId) {
        return memberService.getMyRole(userId);
    }
}
