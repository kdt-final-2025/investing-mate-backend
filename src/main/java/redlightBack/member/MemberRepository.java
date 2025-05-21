package redlightBack.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redlightBack.member.memberEntity.Member;
import redlightBack.member.memberEntity.Role;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);

    boolean existsByUserIdAndRole(String userId, Role role);
}
