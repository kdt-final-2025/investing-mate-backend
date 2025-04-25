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

    // 2) 기자 승격 – PATCH, 200 OK
    @PatchMapping("/{userId}/promote")
    @ResponseStatus(HttpStatus.OK)
    public MemberResponseDto promoteToReporter(@LoginMemberId String userId) {
        return memberService.promoteToReporter(userId);
    }
}
