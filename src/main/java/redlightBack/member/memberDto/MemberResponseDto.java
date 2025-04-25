package redlightBack.member.memberDto;

import redlightBack.member.Role;
import redlightBack.member.Member;

/**
 * 사용자 정보 응답용 DTO
 */
public record MemberResponseDto(
        String userId,
        String email,
        String Name,
        Role role
) {
    public static MemberResponseDto fromEntity(Member m) {
        return new MemberResponseDto(
                m.getUserId(),
                m.getEmail(),
                m.getFullname(),
                m.getRole()
        );
    }
}
