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
import redlightBack.Post.Dto.CreatePostRequest;
import redlightBack.Post.Dto.DetailPostResponse;
import redlightBack.Post.Dto.PostResponse;

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

    @DisplayName("게시글 생성 테스트")
    @Test
    public void 게시글_생성_테스트(){

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";
        BoardResponse testBoard = createBoard("자유게시판");
        Long boardId = testBoard.id();

        PostResponse postResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(new CreatePostRequest(boardId, "게시물 제목", "게시물 내용", List.of("url1", "url2", "url3")))
                .when()
                .post("posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostResponse.class);


        assertThat(postResponse.boardId()).isEqualTo(boardId);
        assertThat(postResponse.postTitle()).isEqualTo("게시물 제목");
        assertThat(postResponse.content()).isEqualTo("게시물 내용");
        assertThat(postResponse.imageUrls()).isEqualTo(List.of("url1", "url2", "url3"));
    }

    @DisplayName("게시물 조회 테스트")
    @Test
    public void 게시물_조회_테스트 (){

        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목", "내용", List.of("img1", "img2", "img3")));
        Long postId = post.id();


        DetailPostResponse detailPostResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .get("/posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(DetailPostResponse.class);

        assertThat(detailPostResponse.imageUrls().size()).isEqualTo(3);
        assertThat(detailPostResponse.postTitle()).isEqualTo("제목");
        assertThat(detailPostResponse.content()).isEqualTo("내용");
    }

    @DisplayName("게시물 수정 테스트")
    @Test
    public void 게시물_수정_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목", "내용", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        PostResponse postResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("postId", postId)
                .body(new CreatePostRequest(boardId, "수정된제목", "수정된내용", List.of("수정이미지1", "수정이미지2", "수정이미지3", "수정이미지4")))
                .when()
                .put("posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostResponse.class);

        assertThat(postResponse.postTitle()).isEqualTo("수정된제목");
        assertThat(postResponse.content()).isEqualTo("수정된내용");
        assertThat(postResponse.imageUrls().size()).isEqualTo(4);

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

    public PostResponse createPost(CreatePostRequest request){

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";


        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(new CreatePostRequest(request.boardId(), request.postTitle(), request.content(), List.of("img1", "img2", "img3")))
                .when()
                .post("/posts")
                .then().log().all()
                .extract()
                .as(PostResponse.class);
    }

    public DetailPostResponse getDetail(Long postId){

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .get("post/{postId}")
                .then().log().all()
                .extract()
                .as(DetailPostResponse.class);
    }
}
