package redlightBack.member.memberDto;

// 관리자인지 여부만 반환하는 DTO
public record AdminResponseDto(
        boolean isAdmin
) {}
