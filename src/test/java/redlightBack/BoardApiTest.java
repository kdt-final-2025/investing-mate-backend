package redlightBack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import redlightBack.Board.Dto.BoardResponse;
import redlightBack.Board.Dto.CreateBoardRequest;
import redlightBack.Comment.Dto.CreateCommentRequest;
import redlightBack.Post.Dto.*;
import redlightBack.Post.Enum.Direction;
import redlightBack.Post.Enum.SortBy;

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
        String token = generateTestToken();

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

        String token = generateTestToken();
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


        PostResponse detailPostResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .get("/posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostResponse.class);

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

        String token = generateTestToken();

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

    @DisplayName("게시물 삭제 테스트")
    @Test
    public void 게시물_삭제_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목", "내용", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        String token = generateTestToken();

        DeletePostResponse deletePostResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("postId", postId)
                .when()
                .delete("/posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(DeletePostResponse.class);

        assertThat(deletePostResponse.deletedAt()).isNotNull();
    }

    @DisplayName("게시물 목록 조회 테스트")
    @Test
    public void 게시물_목록_조회_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post1 = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        PostResponse post2 = createPost(new CreatePostRequest(boardId, "제목2", "내용2", List.of("img1", "img2", "img3")));
        PostResponse post3 = createPost(new CreatePostRequest(boardId, "제목3", "내용3", List.of("img1", "img2", "img3")));
        PostResponse post4 = createPost(new CreatePostRequest(boardId, "제목4", "내용4", List.of("img1", "img2", "img3")));
        PostResponse post5 = createPost(new CreatePostRequest(boardId, "제목5", "내용5", List.of("img1", "img2", "img3")));
        PostResponse post6 = createPost(new CreatePostRequest(boardId, "제목6", "내용6", List.of("img1", "img2", "img3")));
        PostResponse post7 = createPost(new CreatePostRequest(boardId, "제목7", "내용7", List.of("img1", "img2", "img3")));
        PostResponse post8 = createPost(new CreatePostRequest(boardId, "제목8", "내용8", List.of("img1", "img2", "img3")));

        deletePost(post8.id());

        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.size()).isEqualTo(7);
    }

    @DisplayName("게시물 검색 테스트")
    @Test
    public void 게시물_검색_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post1 = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        PostResponse post2 = createPost(new CreatePostRequest(boardId, "제목2", "내용2", List.of("img1", "img2", "img3")));
        PostResponse post3 = createPost(new CreatePostRequest(boardId, "제목3", "내용3", List.of("img1", "img2", "img3")));
        PostResponse post4 = createPost(new CreatePostRequest(boardId, "제목4", "내용4", List.of("img1", "img2", "img3")));
        PostResponse post5 = createPost(new CreatePostRequest(boardId, "제목5", "내용5", List.of("img1", "img2", "img3")));
        PostResponse post6 = createPost(new CreatePostRequest(boardId, "제목6", "내용6", List.of("img1", "img2", "img3")));
        PostResponse post7 = createPost(new CreatePostRequest(boardId, "제목7", "내용7", List.of("img1", "img2", "img3")));
        PostResponse post8 = createPost(new CreatePostRequest(boardId, "제목8", "내용8", List.of("img1", "img2", "img3")));

        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .queryParam("postTitle", "제목1")
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.size()).isEqualTo(1);
        assertThat(postListResponses).anyMatch(post -> post.postTitle().equals("제목1"));
    }

    @DisplayName("게시물 정렬 테스트")
    @Test
    public void 게시물_정렬_테스트_내림차순 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post1 = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        PostResponse post2 = createPost(new CreatePostRequest(boardId, "제목2", "내용2", List.of("img1", "img2", "img3")));
        PostResponse post3 = createPost(new CreatePostRequest(boardId, "제목3", "내용3", List.of("img1", "img2", "img3")));
        PostResponse post4 = createPost(new CreatePostRequest(boardId, "제목4", "내용4", List.of("img1", "img2", "img3")));
        PostResponse post5 = createPost(new CreatePostRequest(boardId, "제목5", "내용5", List.of("img1", "img2", "img3")));
        PostResponse post6 = createPost(new CreatePostRequest(boardId, "제목6", "내용6", List.of("img1", "img2", "img3")));
        PostResponse post7 = createPost(new CreatePostRequest(boardId, "제목7", "내용7", List.of("img1", "img2", "img3")));
        PostResponse post8 = createPost(new CreatePostRequest(boardId, "제목8", "내용8", List.of("img1", "img2", "img3")));

        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .queryParam("sortBy", SortBy.NEWEST)
                .queryParam("direction", Direction.DESC)
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.get(0).postTitle()).isEqualTo("제목8");

    }

    @DisplayName("게시물 정렬 테스트")
    @Test
    public void 게시물_정렬_테스트_오름차순 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post1 = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        PostResponse post2 = createPost(new CreatePostRequest(boardId, "제목2", "내용2", List.of("img1", "img2", "img3")));
        PostResponse post3 = createPost(new CreatePostRequest(boardId, "제목3", "내용3", List.of("img1", "img2", "img3")));
        PostResponse post4 = createPost(new CreatePostRequest(boardId, "제목4", "내용4", List.of("img1", "img2", "img3")));
        PostResponse post5 = createPost(new CreatePostRequest(boardId, "제목5", "내용5", List.of("img1", "img2", "img3")));
        PostResponse post6 = createPost(new CreatePostRequest(boardId, "제목6", "내용6", List.of("img1", "img2", "img3")));
        PostResponse post7 = createPost(new CreatePostRequest(boardId, "제목7", "내용7", List.of("img1", "img2", "img3")));
        PostResponse post8 = createPost(new CreatePostRequest(boardId, "제목8", "내용8", List.of("img1", "img2", "img3")));

        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .queryParam("sortBy", SortBy.NEWEST)
                .queryParam("direction", Direction.ASC)
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.get(0).postTitle()).isEqualTo("제목1");

    }

//    @Test
//    public void 댓글_생성_테스트(){
//        BoardResponse board = createBoard("게시판이름");
//        Long boardId = board.id();
//        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
//        Long postId = post.id();
//
//        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMiIsImlhdCI6MTUxNjIzOTAyMn0.d4-EAJSW6QZLPsrJBMf2plzsYMpqSa0xkn5u-1rhYrs"; // 실제 JWT 토큰
//
//        CreateCommentRequest commentRequest = new CreateCommentRequest(post.id(), null, "This is a test comment.");
//
//        RestAssured.given().log().all()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + validJwtToken)
//                .body(commentRequest)
//                .when()
//                .post("/comments")
//                .then().log().all()
//                .statusCode(200);
//    }

    @Test
    public void 댓글_생성_테스트() {
        // 1. 게시판 생성
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();  // 여기까지 OK

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMiIsImlhdCI6MTUxNjIzOTAyMn0.d4-EAJSW6QZLPsrJBMf2plzsYMpqSa0xkn5u-1rhYrs";

        // 4. 댓글 작성 요청 (postId만 넘김)
        CreateCommentRequest commentRequest = new CreateCommentRequest(
                postId,     // postId: 게시글 ID를 넘긴다
                null,       // parentId: null (댓글이니까 부모 없음)
                "This is a test comment."  // 내용
        );

        // 5. API 호출 및 검증
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .body(commentRequest)
                .when()
                .post("/comments")
                .then().log().all()
                .statusCode(200);
    }




    public BoardResponse createBoard(String boardName){

        String token = generateTestToken();

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

        String token = generateTestToken();

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

    public PostResponse getDetail(Long postId){

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .get("posts/{postId}")
                .then().log().all()
                .extract()
                .as(PostResponse.class);
    }

    public DeletePostResponse deletePost (Long postId){

        String token = generateTestToken();

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("postId", postId)
                .delete("/posts/{postId}")
                .then().log().all()
                .extract()
                .as(DeletePostResponse.class);
    }

    public String generateTestToken(){
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";
    }
}
