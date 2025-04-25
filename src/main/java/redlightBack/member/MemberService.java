package redlightBack.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.member.memberDto.MemberResponseDto;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;

    /**
     * 호출 즉시 해당 userId의 역할을 REPORTER로 변경하고
     * 변경된 Member를 DTO로 반환
     */
    @Transactional
    public MemberResponseDto promoteToReporter(String userId) {
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        if (member.getRole() != Role.REPORTER) {
            member.upgradeToReporter();
        }
        return MemberResponseDto.fromEntity(member);
    }
}
