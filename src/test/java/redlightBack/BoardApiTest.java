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
import redlightBack.comment.dto.CommentResponse;
import redlightBack.comment.dto.CreateCommentRequest;
import redlightBack.Post.Dto.*;
import redlightBack.Post.Enum.Direction;
import redlightBack.Post.Enum.SortBy;
import redlightBack.member.memberEntity.Member;

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
    public void Board_Create_test() {

        //테스트용 토큰
        String token = generateTestTokens("user1");
        createMember("user1");

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
    public void 게시판_목록_조회_테스트() {

        createMember("user1");
        createMember("user3");

        BoardResponse board = createBoard("자유게시판");
        Long boardId = board.id();
        BoardResponse board2 = createBoard("공지게시판");
        BoardResponse board3 = createBoard("주식게시판");

        PostResponse post1 = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        PostResponse post2 = createPost(new CreatePostRequest(boardId, "제목2", "내용2", List.of("img1", "img2", "img3")));
        PostResponse post3 = createPost(new CreatePostRequest(boardId, "제목3", "내용3", List.of("img1", "img2", "img3")));
        PostResponse post4 = createPost(new CreatePostRequest(boardId, "제목4", "내용4", List.of("img1", "img2", "img3")));
        PostResponse post5 = createPost(new CreatePostRequest(boardId, "제목5", "내용5", List.of("img1", "img2", "img3")));
        PostResponse post6 = createPost(new CreatePostRequest(boardId, "제목6", "내용6", List.of("img1", "img2", "img3")));
        PostResponse post7 = createPost(new CreatePostRequest(boardId, "제목7", "내용7", List.of("img1", "img2", "img3")));
        PostResponse post8 = createPost(new CreatePostRequest(boardId, "제목8", "내용8", List.of("img1", "img2", "img3")));



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
    public void 게시글_생성_테스트() {

        createMember("user1");

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
    public void 게시물_조회_테스트() {

        createMember("user1");

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

    public void 게시물_수정_테스트() {


        createMember("user1");


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
    public void 게시물_삭제_테스트() {


        createMember("user1");

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

    public void 게시물_목록_조회_테스트() {


        createMember("user1");

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

    @DisplayName("게시물 조회수 증가 테스트")
    @Test
    public void 게시물_조회수_증가_테스트() {
        // 1) 회원 및 게시판·게시글 준비
        createMember("user1");
        BoardResponse board = createBoard("테스트게시판");
        Long boardId = board.id();
        PostResponse post = createPost(new CreatePostRequest(
                boardId, "제목", "내용", List.of("img1", "img2")
        ));
        Long postId = post.id();

        // 2) 첫 번째 조회: viewCount == 1 이어야 함
        PostResponse firstView = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .when()
                .get("/posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract().as(PostResponse.class);

        assertThat(firstView.viewCount()).isEqualTo(1);

        // 3) 두 번째 조회: viewCount == 2 이어야 함
        PostResponse secondView = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .when()
                .get("/posts/{postId}")
                .then().log().all()
                .statusCode(200)
                .extract().as(PostResponse.class);

        assertThat(secondView.viewCount()).isEqualTo(2);
    }

    @DisplayName("게시물 검색 테스트")
    @Test
    public void 게시물_검색_테스트() {


        createMember("user1");

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
    public void 게시물_정렬_테스트_내림차순() {


        createMember("user1");

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
    public void 게시물_정렬_테스트_오름차순() {


        createMember("user1");

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

        createMember("user1");
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

        createMember("user1");
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

        createMember("user1");
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

        createMember("user1");
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

        createMember("user1");
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

        createMember("user1");
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

        createMember("user1");

        String token = generateTestTokens("user6");
        createMember("user6");

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



        PostsLikedAndPagingResponse likedPosts = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/posts/liked")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PostsLikedAndPagingResponse.class);

        List<PostsLikedResponse> responses = likedPosts.likedPostsResponse();

        assertThat(responses.size()).isEqualTo(3);
        assertThat(responses.get(0).postTitle()).isEqualTo("제목8");
        assertThat(responses.get(1).postTitle()).isEqualTo("제목3");
        assertThat(responses.get(2).postTitle()).isEqualTo("제목2");

    }


    @Test
    public void 댓글_생성_테스트() {
        // 1. 게시판 생성
        createMember("user1");
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
                .statusCode(200)
                .extract()
                .as(CommentResponse.class);
    }

    @Test
    public void 대댓글_생성_테스트() {
        // 1. 게시판 생성
        createMember("user1");
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMiIsImlhdCI6MTUxNjIzOTAyMn0.d4-EAJSW6QZLPsrJBMf2plzsYMpqSa0xkn5u-1rhYrs";

        // 4. 일반 댓글 작성
        CreateCommentRequest commentRequest = new CreateCommentRequest(
                postId,
                null,
                "This is a test comment."
        );

        CommentResponse parentComment = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .body(commentRequest)
                .when()
                .post("/comments")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CommentResponse.class);

        CreateCommentRequest childCommentRequest = new CreateCommentRequest(
                postId,
                parentComment.commentId(),
                "이것은 대댓글입니다."
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .body(childCommentRequest)
                .when()
                .post("/comments")
                .then().log().all()
                .statusCode(200);

        CreateCommentRequest childCommentRequest2 = new CreateCommentRequest(
                postId,
                parentComment.commentId(),
                "이것은 대댓글입니다2."
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .body(childCommentRequest2)
                .when()
                .post("/comments")
                .then().log().all()
                .statusCode(200);
    }



    @Test
    public void 댓글_수정_테스트() {
        createMember("user1");
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        // 4. 일반 댓글 작성
        CommentResponse parentComment = createComment(new CreateCommentRequest(postId, null, "부모 댓글"));

        // 5. 대댓글 작성 및 ID 저장
        CommentResponse createdComment = createComment(new CreateCommentRequest(postId, parentComment.commentId(), "이것은 대댓글입니다2."));
        System.out.println("Created comment ID = " + createdComment.commentId());

        // 6. 대댓글 수정 요청
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .body(new CreateCommentRequest(postId, parentComment.commentId(), "수정된 내용"))
                .pathParam("commentId", createdComment.commentId())  // 이름 일치
                .when()
                .put("/comments/{commentId}")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void 댓글_삭제_테스트() {
        createMember("user1");
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        // 4. 일반 댓글 작성
        CommentResponse parentComment = createComment(new CreateCommentRequest(postId, null, "부모 댓글"));

        // 5. 대댓글 작성 및 ID 저장
        CommentResponse createdComment = createComment(new CreateCommentRequest(postId, parentComment.commentId(), "이것은 대댓글입니다2."));
        System.out.println("Created comment ID = " + createdComment.commentId());

        // 6. 대댓글 삭제 요청 (첫 번째)
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId",1L)
                .when()
                .delete("/comments/{commentId}" )
                .then().log().all()
                .statusCode(200);  // 정상 삭제

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId",2L)
                .when()
                .delete("/comments/{commentId}" )
                .then().log().all()
                .statusCode(200);  // 정상 삭제



        // 7. 좋아요순 댓글 + 대댓글 조회 API 호출 및 검증
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("postId", postId)
                .queryParam("sortType", "LIKE")
                .queryParam("size", 150)
                .queryParam("pageNumber", 1)// 추가: 좋아요 순 정렬
                .when()
                .get("/comments")
                .then().log().all()
                .statusCode(200);
    }


    @Test
    public void 댓글_좋아요_테스트() {
        createMember("user1");
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";

        // 4. 일반 댓글 작성
        CommentResponse parentComment = createComment(new CreateCommentRequest(postId, null, "부모 댓글"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId", parentComment.commentId())  // 이름 일치
                .when()
                .post("/comments/{commentId}/likes")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId", parentComment.commentId())  // 이름 일치
                .when()
                .post("/comments/{commentId}/likes")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void 좋아요순_댓글트리_조회테스트() {
        // 1. 게시판 생성
        createMember("user1");
        BoardResponse board = createBoard("게시판이름");
        Long boardId = board.id();

        // 2. 게시글 생성
        PostResponse post = createPost(new CreatePostRequest(boardId, "제목1", "내용1", List.of("img1", "img2", "img3")));
        Long postId = post.id();

        // 3. JWT 토큰 준비
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMiIsImlhdCI6MTUxNjIzOTAyMn0.d4-EAJSW6QZLPsrJBMf2plzsYMpqSa0xkn5u-1rhYrs";

        // 4. 부모 댓글 작성
        CreateCommentRequest parentRequest = new CreateCommentRequest(postId, null, "부모 댓글");
        CommentResponse parentComment = createComment(parentRequest);
        //4.1 부모 댓글 2 작성
        CreateCommentRequest parentRequest2 = new CreateCommentRequest(postId, null, "부모 댓글2");
        CommentResponse parentComment2 = createComment(parentRequest2);
        //4.2 부모댓글 3 작성
        CreateCommentRequest parentRequest3 = new CreateCommentRequest(postId, null, "부모 댓글2");
        CommentResponse parentComment3 = createComment(parentRequest3);

        // 5. 대댓글 1 작성
        CreateCommentRequest childRequest1 = new CreateCommentRequest(postId, parentComment.commentId(), "대댓글 1");
        CommentResponse childComment1 = createComment(childRequest1);

        // 6. 대댓글 2 작성
        CreateCommentRequest childRequest2 = new CreateCommentRequest(postId, parentComment.commentId(), "대댓글 2");
        CommentResponse childComment2 = createComment(childRequest2);

        //부모 댓글 2 좋아요
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId", parentComment2.commentId())  // 이름 일치
                .when()
                .post("/comments/{commentId}/likes")
                .then().log().all()
                .statusCode(200);
        //부모 댓글 3 좋아요
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId", parentComment3.commentId())  // 이름 일치
                .when()
                .post("/comments/{commentId}/likes")
                .then().log().all()
                .statusCode(200);

        //대댓글 2 좋아요
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validJwtToken)
                .pathParam("commentId", childComment2.commentId())  // 이름 일치
                .when()
                .post("/comments/{commentId}/likes")
                .then().log().all()
                .statusCode(200);


        // 7. 좋아요순 댓글 + 대댓글 조회 API 호출 및 검증
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("postId", postId)
                .queryParam("sortType", "LIKE")
                .queryParam("size", 150)
                .queryParam("pageNumber", 1)// 추가: 좋아요 순 정렬
                .when()
                .get("/comments")
                .then().log().all()
                .statusCode(200);


    }

    public BoardResponse createBoard(String boardName) {

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

    public PostResponse createPost(CreatePostRequest request) {

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

    public PostResponse getDetail(Long postId) {

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("postId", postId)
                .get("posts/{postId}")
                .then().log().all()
                .extract()
                .as(PostResponse.class);
    }

    public DeletePostResponse deletePost(Long postId) {

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


    public String generateTestToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNzQ1NDUyODAwLCJleHAiOjE3NzY5ODg4MDB9.P4f4xRaylLo8QXIqDxW8dFlLAEITtJr-hep4Ohyh42U";
    }

      public CommentResponse createComment(CreateCommentRequest commentRequest) {

          String token = generateTestToken();

          return RestAssured.given().log().all()
                  .contentType(ContentType.JSON)
                  .header("Authorization", "Bearer " + token)
                  .body(commentRequest)
                  .when()
                  .post("/comments")
                  .then().log().all()
                  .statusCode(200)
                  .extract()
                  .as(CommentResponse.class);
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

    public Member createMember (String userId){

        String token = generateTestTokens(userId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("member/me")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(Member.class);
    }

    private Map<String, String> tokens = Map.of(
            "user1", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJleGFtcGxlMUBlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.47l3xzbylBcWghRGY2gR9jUsy_gUa4s1wUJLduzvo7Y",
            "user2", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyMzQ1Njc4OTAxIiwiZW1haWwiOiJleGFtcGxlMkBlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.Fr2sLUf7mxbk9vcd0_LzmgCd3MLVNN21FWJamtTkI6U",
            "user3", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzNDU2Nzg5MDEyIiwiZW1haWwiOiJleGFtcGxlM0BlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.TbTxngE22J8J-dBpgGRLY9wxeuK0h9fmQtY78wPqb5s",
            "user4", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0NTY3ODkwMTIzIiwiZW1haWwiOiJleGFtcGxlNEBlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.kuPJaoaDpyKbIRGS9auIZAYXTho05VYndZr59D2gE9I",
            "user5", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1Njc4OTAxMjM0IiwiZW1haWwiOiJleGFtcGxlNUBlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.ZJd5fvaFO_wYVr17bWmCuYZbYJZWRmDR8k_XeC9Olp0",
            "user6", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2Nzg5MDEyMzQ1IiwiZW1haWwiOiJleGFtcGxlNkBlbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.OGQpY8Zri7UWugvOiohjCXdHg14MLn07N1vadRo-tfQ"
    );

    public String generateTestTokens(String userId){
        return tokens.get(userId);

    }
}
