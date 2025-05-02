package redlightBack.Post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import redlightBack.Board.Board;
import redlightBack.Board.BoardRepository;
import redlightBack.Post.Dto.*;
import redlightBack.Post.Enum.Direction;
import redlightBack.Post.Enum.SortBy;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Member;


import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final PostQueryRepository postQueryRepository;
    private final PostMapper postMapper;
    private final MemberRepository memberRepository;
    private final PostLikeQueryRepository postLikeQueryRepository;

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

        return postMapper.toPostResponse(post);
    }

    //게시물 상세조회
    public PostResponse getDetailPost (Long postId){

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        return postMapper.toPostResponse(post);
    }

    //게시물 수정
    @Transactional
    public PostResponse update (String userId, Long postId, CreatePostRequest request){

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        if(!post.getUserId().equals(userId)){
            throw new NoSuchElementException("게시물 수정은 작성자만 할 수 있습니다.");
        }

        post.updatePost(request.postTitle(),
                request.content(),
                request.imageUrls());

        return postMapper.toPostResponse(post);
    }

    //게시물 삭제
    @Transactional
    public DeletePostResponse delete (String userId, Long postId){

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        if(!post.getUserId().equals(userId)){
            throw new NoSuchElementException("게시물 삭제는 작성자만 할 수 있습니다.");
        }

        post.softDelete();

        return new DeletePostResponse(post.getDeletedAt());
    }


    //게시글 목록 조회(제목 검색, userId 검색, 좋아요 정렬, 최신순 정렬)
    public PostListAndPagingResponse getPosts(String userId,
                                              Long boardId,
                                              String postTitle,
                                              SortBy sortBy,
                                              Direction direction,
                                              Pageable pageable) {

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new NoSuchElementException("해당 게시판을 찾을 수 없습니다.")
        );


        int size = pageable.getPageSize();
        Long offset = pageable.getOffset();
        long totalElements = postQueryRepository.countPosts(boardId, postTitle, userId);
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        List<PostDto> posts = postQueryRepository.searchAndOrderingPosts(boardId, postTitle, userId, sortBy, direction, offset, size);

        List<PostListResponse> responseList = posts.stream()
                .map(list -> new PostListResponse(list.id(),
                        list.postTitle(),
                        list.userId(),
                        list.viewCount(),
                        list.commentCount(),
                        list.likeCount(),
                        list.createdAt())
                ).toList();

        PageInfo pageInfo = new PageInfo(pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalElements,
                totalPages
                );

        return new PostListAndPagingResponse(board.getBoardName(), responseList, pageInfo);
    }

    //사용자가 좋아요 누른 게시글 목록 보기
    public PostsLikedAndPagingResponse likedPostList (String userId, Pageable pageable){

        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new NoSuchElementException("유효하지 않은 사용자입니다.")
        );

        int pageNumber = pageable.getPageNumber();
        int size = pageable.getPageSize();
        long offset = pageable.getOffset();
        long totalElements = postLikeQueryRepository.countLikedPosts(userId);
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        List<PostLikeDto> postsLikedByUser = postLikeQueryRepository.findPostsLikedByUser(member.getUserId(), offset, size);

        List<PostsLikedResponse> posts = postMapper.toListPostLikeResponse(postsLikedByUser);

        PageInfo pageInfo = new PageInfo(pageNumber, size, totalElements, totalPages);

        return new PostsLikedAndPagingResponse(posts, pageInfo);
    }

}
