package redlightBack.Board;

import org.springframework.stereotype.Service;
import redlightBack.Board.Dto.BoardResponse;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

//    public BoardResponse create (String userId){
//        Board board = new Board()
//    }


    //게시판 목록조회
    public List<BoardResponse> getBoardList (){
        return boardRepository.findAll().stream().map(
                board -> new BoardResponse(board.getId(), board.getBoardName(), board.postCount)

        ).toList();
    }
}
