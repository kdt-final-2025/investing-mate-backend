package redlightBack.Post;

import org.springframework.stereotype.Service;
import redlightBack.Board.Board;
import redlightBack.Board.BoardRepository;
import redlightBack.Post.Dto.CreatePostRequest;
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






}
