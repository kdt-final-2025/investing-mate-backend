package redlightBack.Board;

import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.CreateBoardResponse;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //게시판 생성
    //TODO 관리자 권한 추가 필요
    public CreateBoardResponse create (String userId, CreateBoardRequest request){
        Board board = new Board(request.boardName());

        boardRepository.save(board);

        return new CreateBoardResponse(board.getId(),
                board.getBoardName());
    }

    //게시판 목록조회
    //TODO post 구현 후에 post 개수 넘겨주는 method 추가
    public List<BoardResponse> getBoardList (){
        return boardRepository.findAll().stream().map(
                board -> new BoardResponse(board.getId(), board.getBoardName(), board.postCount)

        ).toList();
    }
}
