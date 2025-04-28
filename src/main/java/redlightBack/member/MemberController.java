package redlightBack.member;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import redlightBack.member.memberDto.MemberResponseDto;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // POST /members/me
    // - Jwt를 서비스에 넘겨서 upsert 수행

    @PostMapping("/me")
    public MemberResponseDto provisionUser(@AuthenticationPrincipal Jwt jwt) {
        return memberService.provisionUserFromJwt(jwt);
    }
}
