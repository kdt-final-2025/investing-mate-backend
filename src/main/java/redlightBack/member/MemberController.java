package redlightBack.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.member.memberDto.MemberResponseDto;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 2) 기자 승격 – post, 200 OK
    @PostMapping("/{userId}/promote")
    @ResponseStatus(HttpStatus.OK)
    public MemberResponseDto promoteToReporter(@LoginMemberId String userId) {
        return memberService.promoteToReporter(userId);
    }
}
