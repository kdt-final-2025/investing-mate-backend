package redlightBack.member.memberDto;

import redlightBack.member.memberEntity.Role;

// 사용자 정보 응답용 DTO
public record MemberResponseDto(
        String userId,
        String email,
        String fullname,
        Role role
) {
}
