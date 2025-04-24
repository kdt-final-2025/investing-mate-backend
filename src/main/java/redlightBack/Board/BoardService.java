package redlightBack.Board;

import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.BoardCreateResponse;
import redlightBack.Board.Dto.BoardRequest;
import redlightBack.Board.Dto.BoardResponse;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //게시판 생성
    //관리자 권한 추가 필요
    public BoardCreateResponse create (String userId, BoardRequest request){
        Board board = new Board(request.boardName());

        boardRepository.save(board);

        return new BoardCreateResponse(board.getId(),
                board.getBoardName());
    }

    //게시판 목록조회
    public List<BoardResponse> getBoardList (){
        return boardRepository.findAll().stream().map(
                board -> new BoardResponse(board.getId(), board.getBoardName(), board.postCount)

        ).toList();
    }
}
