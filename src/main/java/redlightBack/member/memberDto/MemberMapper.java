package redlightBack.member.memberDto;

import redlightBack.member.memberEntity.Member;
import redlightBack.member.memberEntity.Role;

// DTO ↔ Entity 변환만 담당
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
                m.getRole()
        );
    }
}
