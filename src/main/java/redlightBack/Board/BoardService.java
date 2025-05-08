package redlightBack.Board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;
import redlightBack.Post.PostQueryRepository;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Role;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberRepository memberRepository;

    // 게시판 생성 (관리자만)
    public BoardResponse create(String userId, CreateBoardRequest request) {

        // ADMINISTRATOR 권한이 아니면 예외
        if (!memberRepository.existsByUserIdAndRole(userId, Role.ADMINISTRATOR)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        Board board = new Board(request.boardName());
        boardRepository.save(board);

        return new BoardResponse(
                board.getId(),
                board.getBoardName(),
                board.getPostCount()
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
