package redlightBack.Post;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redlightBack.Board.Board;
import redlightBack.Board.BoardRepository;
import redlightBack.Post.Dto.*;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final PostQueryRepository postQueryRepository;

    public PostService(PostRepository postRepository, BoardRepository boardRepository, PostQueryRepository postQueryRepository) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.postQueryRepository = postQueryRepository;
    }

    //게시물 생성
    public PostResponse create (String userId, CreatePostRequest request){

        Board board = boardRepository.findById(request.boardId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 게시판입니다."));

        Post post = new Post(board.getId(),
                request.postTitle(),
                userId,
                request.content(),
                request.imageUrls());

        postRepository.save(post);

        return toPostResponse(post);
    }

    //게시물 상세조회
    public PostResponse getDetailPost (Long postId){

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        return toPostResponse(post);
    }

    //게시물 수정
    @Transactional
    public PostResponse update (String userId, Long postId, CreatePostRequest request){

        //TODO 작성자 검증 추가

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        post.updatePost(request.postTitle(),
                request.content(),
                request.imageUrls());

        return toPostResponse(post);
    }

    //게시물 삭제
    @Transactional
    public DeletePostResponse delete (String userId, Long postId){

        //TODO 작성자 확인 로직

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        post.softDelete();

        return new DeletePostResponse(post.getDeletedAt());
    }


    //게시글 목록 조회(제목 검색, userId 검색, 좋아요 정렬, 최신순 정렬)
    public PostListAndPagingResponse getPosts(String userId,
                                              Long boardId,
                                              String postTitle,
                                              String sortBy,
                                              String direction,
                                              Pageable pageable) {

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new NoSuchElementException("해당 게시판을 찾을 수 없습니다.")
        );


        int size = pageable.getPageSize();
        Long offset = pageable.getOffset();
        long totalElements = postQueryRepository.countPosts(boardId, postTitle, userId);
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        List<Post> posts = postQueryRepository.searchAndOrderingPosts(boardId, postTitle, userId, sortBy, direction, offset, size);

        List<PostListResponse> responseList = posts.stream()
                .map(list -> new PostListResponse(list.getId(),
                        list.getPostTitle(),
                        list.getUserId(),
                        list.getViewCount(),
                        list.getCommentCount(),
                        list.getLikeCount())
                ).toList();

        PageInfo pageInfo = new PageInfo(pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalElements,
                totalPages
                );

        return new PostListAndPagingResponse(board.getBoardName(), responseList, pageInfo);
    }

    public PostResponse toPostResponse (Post post){
        return new PostResponse(post.getBoardId(),
                post.getId(),
                post.getPostTitle(),
                post.getUserId(),
                post.getViewCount(),
                post.getContent(),
                post.getImageUrls(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getLikeCount(),
                post.isLikedByMe(),
                post.getCommentCount());
    }
}
