package redlightBack.board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import redlightBack.board.dto.CreateBoardRequest;
import redlightBack.board.dto.BoardResponse;
import redlightBack.post.PostQueryRepository;
import redlightBack.member.MemberRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberRepository memberRepository;

    //게시판 생성
    public BoardResponse create (String userId, CreateBoardRequest request){
        Board board = new Board(request.boardName());

        memberRepository.findByUserId(userId).orElseThrow(
                () -> new AccessDeniedException("접근 권한이 없습니다.")
        );

        boardRepository.save(board);

        return new BoardResponse(board.getId(),
                board.getBoardName(),
                board.postCount);
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
