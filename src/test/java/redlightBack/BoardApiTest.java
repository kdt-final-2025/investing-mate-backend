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
import redlightBack.Post.Dto.*;
import redlightBack.Post.Enum.Direction;
import redlightBack.Post.Enum.SortBy;

import java.util.List;
import java.util.Map;

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
        String token = generateTestTokens("user1");

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

        String token = generateTestTokens("user1");
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

        String token = generateTestTokens("user1");

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

        String token = generateTestTokens("user1");

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

    @DisplayName("좋아요 테스트")
    @Test
    public void 좋아요_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        String token = generateTestTokens("user1");

        PostLikeResponse postLikeResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/posts/{postId}/like")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostLikeResponse.class);

        assertThat(postLikeResponse.likeCount()).isEqualTo(1);
    }

    @DisplayName("좋아요 취소 테스트")
    @Test
    public void 좋아요_취소_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        String token = generateTestTokens("user1");
        toggleLike(postId, token);
        PostLikeResponse postLikeResponse = toggleLike(postId, token);

        assertThat(postLikeResponse.likeCount()).isEqualTo(0);
        assertThat(postLikeResponse.liked()).isEqualTo(false);
    }

    @DisplayName("여러 사용자의 좋아요 테스트")
    @Test
    public void 여러_사용자의_좋아요_테스트 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        toggleLike(postId, generateTestTokens("user1"));
        toggleLike(postId, generateTestTokens("user2"));
        toggleLike(postId, generateTestTokens("user3"));
        toggleLike(postId, generateTestTokens("user4"));
        toggleLike(postId, generateTestTokens("user5"));
        PostLikeResponse postLikeResponse = toggleLike(postId, generateTestTokens("user6"));

        PostResponse updatedPost = getDetail(postId);

        assertThat(postLikeResponse.likeCount()).isEqualTo(6);
        assertThat(updatedPost.likeCount()).isEqualTo(6);
    }

    @DisplayName("여러 사용자의 좋아요 테스트2")
    @Test
    public void 여러_사용자의_좋아요_테스트2 (){
        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();

        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        toggleLike(postId, generateTestTokens("user1"));
        toggleLike(postId, generateTestTokens("user1"));

        toggleLike(postId, generateTestTokens("user2"));
        toggleLike(postId, generateTestTokens("user2"));

        toggleLike(postId, generateTestTokens("user3"));
        toggleLike(postId, generateTestTokens("user3"));

        toggleLike(postId, generateTestTokens("user4"));
        toggleLike(postId, generateTestTokens("user5"));
        PostLikeResponse postLikeResponse = toggleLike(postId, generateTestTokens("user6"));

        PostResponse updatedPost = getDetail(postId);

        assertThat(postLikeResponse.likeCount()).isEqualTo(3);
        assertThat(updatedPost.likeCount()).isEqualTo(3);
    }

    @DisplayName("좋아요순으로 목록 조회 테스트")
    @Test
    public void 좋아요순으로_목록_조회_테스트 (){
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

        Long postId1 = post1.id();
        Long postId2 = post2.id();
        Long postId3 = post3.id();
        Long postId4 = post4.id();

        toggleLike(postId3, generateTestTokens("user1"));
        toggleLike(postId3, generateTestTokens("user2"));
        toggleLike(postId3, generateTestTokens("user3"));
        toggleLike(postId3, generateTestTokens("user4"));
        toggleLike(postId3, generateTestTokens("user5"));
        toggleLike(postId3, generateTestTokens("user6"));

        toggleLike(postId1, generateTestTokens("user1"));
        toggleLike(postId1, generateTestTokens("user2"));
        toggleLike(postId1, generateTestTokens("user3"));
        toggleLike(postId1, generateTestTokens("user4"));
        toggleLike(postId1, generateTestTokens("user5"));

        toggleLike(postId2, generateTestTokens("user1"));
        toggleLike(postId2, generateTestTokens("user2"));
        toggleLike(postId2, generateTestTokens("user3"));

        toggleLike(postId4, generateTestTokens("user3"));


        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .queryParam("sortBy", SortBy.MOST_LIKED)
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.get(0).postTitle()).isEqualTo("제목3");
        assertThat(postListResponses.get(1).postTitle()).isEqualTo("제목1");
        assertThat(postListResponses.get(2).postTitle()).isEqualTo("제목2");
        assertThat(postListResponses.get(3).postTitle()).isEqualTo("제목4");
        assertThat(postListResponses.get(4).postTitle()).isEqualTo("제목8");
        assertThat(postListResponses.get(5).postTitle()).isEqualTo("제목7");

    }

    @DisplayName("좋아요순 오래된 게시물 순서로 목록 조회")
    @Test
    public void 좋아요와_오래된_게시물_순서로_목록_조회 (){
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

        Long postId8 = post8.id();
        Long postId2 = post2.id();
        Long postId3 = post3.id();

        toggleLike(postId3, generateTestTokens("user1"));
        toggleLike(postId3, generateTestTokens("user2"));
        toggleLike(postId3, generateTestTokens("user3"));
        toggleLike(postId3, generateTestTokens("user4"));
        toggleLike(postId3, generateTestTokens("user5"));
        toggleLike(postId3, generateTestTokens("user6"));

        toggleLike(postId8, generateTestTokens("user1"));
        toggleLike(postId8, generateTestTokens("user2"));
        toggleLike(postId8, generateTestTokens("user3"));
        toggleLike(postId8, generateTestTokens("user4"));
        toggleLike(postId8, generateTestTokens("user5"));
        toggleLike(postId8, generateTestTokens("user6"));

        toggleLike(postId2, generateTestTokens("user1"));
        toggleLike(postId2, generateTestTokens("user2"));
        toggleLike(postId2, generateTestTokens("user3"));
        toggleLike(postId2, generateTestTokens("user4"));
        toggleLike(postId2, generateTestTokens("user5"));
        toggleLike(postId2, generateTestTokens("user6"));


        PostListAndPagingResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("boardId", boardId)
                .queryParam("sortBy", SortBy.MOST_LIKED)
                .queryParam("direction", Direction.ASC)
                .when()
                .get("/posts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostListAndPagingResponse.class);

        List<PostListResponse> postListResponses = response.postListResponse();

        assertThat(postListResponses.get(0).postTitle()).isEqualTo("제목2");
        assertThat(postListResponses.get(1).postTitle()).isEqualTo("제목3");
        assertThat(postListResponses.get(2).postTitle()).isEqualTo("제목8");
        assertThat(postListResponses.get(3).postTitle()).isEqualTo("제목1");
        assertThat(postListResponses.get(4).postTitle()).isEqualTo("제목4");
        assertThat(postListResponses.get(5).postTitle()).isEqualTo("제목5");

    }

    @DisplayName("사용자가 좋아요 누른 게시글 목록 보기")
    @Test
    public void 사용자가_좋아요_누른_게시글_목록_보기 (){

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

        Long postId8 = post8.id();
        Long postId2 = post2.id();
        Long postId3 = post3.id();

        toggleLike(postId8, generateTestTokens("user6"));
        toggleLike(postId2, generateTestTokens("user6"));
        toggleLike(postId3, generateTestTokens("user6"));

        String token = generateTestTokens("user6");

        LikedPostListAndPagingResponse likedPosts = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/boards/liked")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LikedPostListAndPagingResponse.class);

        List<LikedPostListResponse> responses = likedPosts.likedPosts();

        assertThat(responses.size()).isEqualTo(3);
        assertThat(responses.get(0).postTitle()).isEqualTo("제목8");
        assertThat(responses.get(1).postTitle()).isEqualTo("제목3");
        assertThat(responses.get(2).postTitle()).isEqualTo("제목2");

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

    public PostLikeResponse toggleLike(Long postId, String token){

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/posts/{postId}/like")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostLikeResponse.class);
    }



    public String generateTestToken(){
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";
    }

    private Map<String, String> tokens = Map.of(
            "user1", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U",
            "user2", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyMzQ1Njc4OTAxIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzQ1ODM5MjAwLCJleHAiOjE3NzczNzUyMDB9.w7PfX7l9nZnK4VX2vAWoWE93_PXQpoYGlURuDzWjd_M",
            "user3", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzNDU2Nzg5MDEyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzQ1ODM5MjAwLCJleHAiOjE3NzczNzUyMDB9.ciysHp4jkmva9rXmhQ_E99Uw-3dGfMhsWblTwEzmVpQ",
            "user4", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0NTY3ODkwMTIzIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzQ1ODM5MjAwLCJleHAiOjE3NzczNzUyMDB9.SQgt9bGWKm7x72RiyzbcrWVZNek5KTkQ48gXnNF7k74",
            "user5", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1Njc4OTAxMjM0IiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzQ1ODM5MjAwLCJleHAiOjE3NzczNzUyMDB9.HRAWZGvX_iETUKrd9FWBuP_3OE3k6Iiey4_naENWbsw",
            "user6", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2Nzg5MDEyMzQ1IiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzQ1ODM5MjAwLCJleHAiOjE3NzczNzUyMDB9.hnn1LeD1f9pZ8KWMqlPkUmugJmcwSzjEtfP--ht9Fn0"
    );

    public String generateTestTokens(String userId){
        return tokens.get(userId);
    }
}
