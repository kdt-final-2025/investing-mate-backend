package redlightBack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import redlightBack.Board.Dto.BoardCreateResponse;
import redlightBack.Board.Dto.BoardRequest;
import redlightBack.Board.Dto.BoardResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoardApiTest {

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
        BoardCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new BoardRequest("자유게시판"))
                .when()
                .post("boards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(BoardCreateResponse.class);

        assertThat(response.boardName()).isEqualTo("자유게시판");
    }

    @DisplayName("게시판 목록 조회 테스트")
    @Test
    public void 게시판_목록_조회_테스트(){

        BoardCreateResponse board1 = createBoard("자유게시판");
        BoardCreateResponse board2 = createBoard("공지게시판");
        BoardCreateResponse board3 = createBoard("주식게시판");


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


    public BoardCreateResponse createBoard(String boardName){
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new BoardRequest(boardName))
                .when()
                .post("boards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(BoardCreateResponse.class);
    }
}
