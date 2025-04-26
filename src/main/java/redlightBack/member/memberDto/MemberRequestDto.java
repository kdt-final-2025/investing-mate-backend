package redlightBack.member.memberDto;

/**
 * 소셜 로그인 직후 사용자 프로비저닝 요청용 DTO
 */
public record MemberRequestDto(
        String userId,     // 추가
        String email,
        String fullname
) {}
