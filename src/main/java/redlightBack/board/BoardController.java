package redlightBack.board;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.board.dto.CreateBoardRequest;
import redlightBack.board.dto.BoardResponse;
import redlightBack.loginUtils.LoginMemberId;

import java.util.List;

@RestController
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }


    //게시판 생성
    @PostMapping("/boards")
    public BoardResponse createBoard(@LoginMemberId String userId,
                                           @RequestBody CreateBoardRequest request){
       return boardService.create(userId, request);
    }


    //게시판 목록 조회
    @GetMapping("/boards")
    public List<BoardResponse> getBoards (){
        return boardService.getBoardList();
    }

}
