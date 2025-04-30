package redlightBack.Board;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;

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
    //TODO post 구현 후에 post 개수 넘겨주는 method 추가
    public List<BoardResponse> getBoardList (){
        return boardRepository.findAll().stream().map(
                board -> new BoardResponse(board.getId(), board.getBoardName(), board.postCount)

        ).toList();
    }
}
