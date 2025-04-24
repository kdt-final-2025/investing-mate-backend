package redlightBack.Board;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.Board.Dto.CreateBoardResponse;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;
import redlightBack.loginUtils.LoginMemberId;

import java.util.List;

@RestController
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }


    //게시판 생성
    //TODO @LoginMemberId 추가 필요,
    @PostMapping("/boards")
    public CreateBoardResponse createBoard(@LoginMemberId String userId,
                                           @RequestBody CreateBoardRequest request){
       return boardService.create(userId, request);
    }


    //게시판 목록 조회
    @GetMapping("/boards")
    public List<BoardResponse> getBoards (){
        return boardService.getBoardList();
    }

}
