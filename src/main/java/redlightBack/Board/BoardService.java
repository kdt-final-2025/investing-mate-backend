package redlightBack.Board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;
import redlightBack.Post.PostQueryRepository;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Member;
import redlightBack.member.memberEntity.Role;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberRepository memberRepository;

    // 게시판 생성 (관리자만 가능)
    public BoardResponse create(String userId, CreateBoardRequest request) {
        // 1) 사용자 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("접근 권한이 없습니다."));  // 로그인 여부 확인 :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}:contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}

        // 2) 관리자 권한 체크
        if (member.getRole() != Role.ADMINISTRATOR) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");  // 권한 없으면 예외
        }

        // 3) 권한 통과 시 게시판 생성
        Board board = new Board(request.boardName());
        boardRepository.save(board);

        return new BoardResponse(
                board.getId(),
                board.getBoardName(),
                board.postCount
        );
    }

    //게시판 목록조회
    public List<BoardResponse> getBoardList (){
        return boardRepository.findAll().stream().map(
                board -> new BoardResponse(board.getId(),
                        board.getBoardName(),
                        (int)postQueryRepository.countPosts(board.getId(), null, null))

        ).toList();
    }
}
