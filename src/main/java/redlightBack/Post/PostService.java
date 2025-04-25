package redlightBack.Post;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import redlightBack.Board.Board;
import redlightBack.Board.BoardRepository;
import redlightBack.Post.Dto.CreatePostRequest;
import redlightBack.Post.Dto.DeletePostResponse;
import redlightBack.Post.Dto.DetailPostResponse;
import redlightBack.Post.Dto.PostResponse;

import static redlightBack.Post.QPost.post;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    public PostService(PostRepository postRepository, BoardRepository boardRepository) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
    }

    //게시물 생성
    public PostResponse create (String userId, CreatePostRequest request){

        Board board = boardRepository.findById(request.boardId()).orElseThrow(
                () -> new RuntimeException("존재하지 않는 게시판입니다."));


        Post post = new Post(request.boardId(),
                request.postTitle(),
                userId,
                request.content(),
                request.imageUrls());

        //이미지 첨부 5개 제한
        post.limitationOfImages(request.imageUrls());

        postRepository.save(post);

        return new PostResponse(board.getId(),
                post.getId(),
                userId,
                post.getPostTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    //게시물 상세조회
    public DetailPostResponse getDetailPost (Long postId){

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
                () -> new RuntimeException("해당 게시물이 존재하지 않습니다.")
        );

        return new DetailPostResponse(post.getId(),
                post.getPostTitle(),
                post.getUserId(),
                post.getViewCount(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt(),
                post.getLikeCount(),
                post.isLikedByMe(),
                post.getCommentCount());

    }

    //게시물 수정
    @Transactional
    public PostResponse update (String userId, Long postId, CreatePostRequest request){

        //TODO 작성자 검증 추가

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("해당 게시물이 존재하지 않습니다.")
        );

        post.updatePost(request.postTitle(),
                request.content(),
                request.imageUrls());

        return new PostResponse(post.getBoardId(),
                post.getId(),
                userId,
                post.getPostTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    //게시물 삭제
    @Transactional
    public DeletePostResponse delete (String userId, Long postId){

        //TODO 작성자 확인 로직

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("해당 게시물이 존재하지 않습니다.")
        );

        post.softDelete();

        return new DeletePostResponse(post.getId(),
                post.getDeletedAt());
    }





}
