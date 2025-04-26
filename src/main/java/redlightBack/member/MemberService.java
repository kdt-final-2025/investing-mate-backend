// src/main/java/redlightBack/member/MemberService.java
package redlightBack.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.member.memberDto.MemberMapper;
import redlightBack.member.memberDto.MemberRequestDto;
import redlightBack.member.memberDto.MemberResponseDto;

@Service
public class MemberService {

    private final MemberRepository memberRepo;

    public MemberService(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }


    // JWT에서 뽑아온 userId/email/fullname 으로 사용자 Upsert
    @Transactional
    public MemberResponseDto provisionUser(MemberRequestDto req) {
        // userId까지 포함된 DTO로 조회/생성
        Member member = memberRepo.findByUserId(req.userId())
                .orElseGet(() -> MemberMapper.toEntity(req));

        // 프로필(email, fullname) 업데이트
        member.updateProfile(req.email(), req.fullname());

        // save()로 insert 혹은 update
        Member saved = memberRepo.save(member);
        return MemberMapper.toResponseDto(saved);
    }

    //일반 사용자 → 기자로 권한 승격
    @Transactional
    public MemberResponseDto promoteToReporter(String userId) {
        Member member = memberRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        member.upgradeToReporter();
        return MemberMapper.toResponseDto(member);
    }
}
