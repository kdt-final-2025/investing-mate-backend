package redlightBack.member.memberDto;

import redlightBack.member.Member;
import redlightBack.member.Role;

public class MemberMapper {
    public static Member toEntity(MemberRequestDto req) {
        return Member.builder()
                .userId(req.userId())
                .email(req.email())
                .fullname(req.fullname())
                .role(Role.GENERAL)
                .build();
    }

    public static MemberResponseDto toResponseDto(Member m) {
        return new MemberResponseDto(
                m.getUserId(),
                m.getEmail(),
                m.getFullname(),
                m.getRole().name()
        );
    }
}
