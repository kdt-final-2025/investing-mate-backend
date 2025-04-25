package redlightBack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Board.Dto.BoardResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BoardApiTest extends AcceptanceTest {

    @LocalServerPort
    int port;

    @Autowired
    DatabaseCleanup databaseCleanup;


    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleanup.execute();
    }

    @DisplayName("게시판 생성 테스트")
    @Test
    public void Board_Create_test(){

        //테스트용 토큰
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        BoardResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(new CreateBoardRequest("자유게시판"))
                .when()
                .post("boards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(BoardResponse.class);

        assertThat(response.boardName()).isEqualTo("자유게시판");
    }

    @DisplayName("게시판 목록 조회 테스트")
    @Test
    public void 게시판_목록_조회_테스트(){

        BoardResponse board1 = createBoard("자유게시판");
        BoardResponse board2 = createBoard("공지게시판");
        BoardResponse board3 = createBoard("주식게시판");


        List<BoardResponse> boards = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when()
                .get("boards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", BoardResponse.class);

        assertThat(boards.size()).isEqualTo(3);
        assertThat(boards).anyMatch(boardResponse
                -> boardResponse.boardName().equals("자유게시판"));
        assertThat(boards).anyMatch(boardResponse
                -> boardResponse.boardName().equals("공지게시판"));
        assertThat(boards).anyMatch(boardResponse
                -> boardResponse.boardName().equals("주식게시판"));
    }


    public BoardResponse createBoard(String boardName){

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(new CreateBoardRequest(boardName))
                .when()
                .post("boards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(BoardResponse.class);
    }
}
