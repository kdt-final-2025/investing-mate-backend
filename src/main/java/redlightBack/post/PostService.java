package redlightBack.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.board.Board;
import redlightBack.board.BoardRepository;
import redlightBack.member.MemberRepository;
import redlightBack.member.memberEntity.Member;
import redlightBack.post.document.PostDocument;
import redlightBack.post.dto.*;
import redlightBack.post.enums.Direction;
import redlightBack.post.enums.SortBy;
import redlightBack.post.repository.PostDocumentRepository;

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
    private final PostDocumentRepository postDocumentRepository;
    private final ElasticsearchOperations esOps;

    //게시물 생성
    public PostResponse create(String userId, CreatePostRequest request) {

        Board board = boardRepository.findById(request.boardId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 게시판입니다."));

        Post post = new Post(board.getId(),
                request.postTitle(),
                userId,
                request.content(),
                request.imageUrls());

        postRepository.save(post);

        // ES에 간단 문서 저장
        postDocumentRepository.save(PostDocument.fromEntity(post));

        return postMapper.toPostResponse(post);
    }

    //게시물 상세조회
    @Transactional
    public PostResponse getDetailPost(Long postId) {
        Post post = postRepository
                .findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NoSuchElementException("해당 게시물이 존재하지 않습니다."));

        post.increaseViewCount();

        return postMapper.toPostResponse(post);
    }

    //게시물 수정
    @Transactional
    public PostResponse update(String userId, Long postId, CreatePostRequest request) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        if (post.isAuthor(userId)) {
            throw new NoSuchElementException("게시물 수정은 작성자만 할 수 있습니다.");
        }

        post.updatePost(request.postTitle(),
                request.content(),
                request.imageUrls());

        // ES에 간단 문서 저장
        postDocumentRepository.save(PostDocument.fromEntity(post));

        return postMapper.toPostResponse(post);
    }

    //게시물 삭제
    @Transactional
    public DeletePostResponse delete(String userId, Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        if (post.isAuthor(userId)) {
            throw new NoSuchElementException("게시물 삭제는 작성자만 할 수 있습니다.");
        }

        post.softDelete();

        return new DeletePostResponse(post.getDeletedAt());
    }


    //게시글 목록 조회(제목 검색, userId 검색, 좋아요 정렬, 최신순 정렬)
    @Transactional(readOnly = true)
    public PostListAndPagingResponse getPosts(
            String userId,
            Long boardId,
            String postTitle,
            SortBy sortBy,
            Direction direction,
            Pageable pageable
    ) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NoSuchElementException("해당 게시판을 찾을 수 없습니다."));

        long totalElements;
        List<PostDto> posts;

        if (postTitle != null && !postTitle.isBlank()) {

            // 1) 정확 문구(phrase) 검색
            String phraseJson = """
                      { "match_phrase": { "postTitle": "%s" } }
                    """.formatted(postTitle);
            StringQuery phraseQuery = new StringQuery(phraseJson);
            List<Long> phraseIds = esOps.search(phraseQuery, PostDocument.class)
                    .map(SearchHit::getContent)
                    .map(PostDocument::getId)
                    .toList();

            List<Long> idList;
            if (!phraseIds.isEmpty()) {
                // 문장 그대로 매칭된 게 있으면 이 결과만
                idList = phraseIds;
            } else {
                // 2) 형태소(token) 검색
                String cleaned = postTitle.replace("\"", "");
                Criteria tokenCrit = Criteria.where("postTitle")
                        .expression(cleaned);
                List<Long> tokenIds = esOps.search(new CriteriaQuery(tokenCrit), PostDocument.class)
                        .map(SearchHit::getContent)
                        .map(PostDocument::getId)
                        .toList();

                if (!tokenIds.isEmpty()) {
                    idList = tokenIds;
                } else {
                    // 3) substring(와일드카드) 검색 – “이” → *이* 매칭
                    Criteria subCrit = Criteria.where("postTitle")
                            .contains(cleaned);
                    idList = esOps.search(new CriteriaQuery(subCrit), PostDocument.class)
                            .map(SearchHit::getContent)
                            .map(PostDocument::getId)
                            .toList();
                }
            }

            // ★ 매칭되는 게 없으면 빈 결과 리턴
            if (idList.isEmpty()) {
                PageInfo emptyPage = new PageInfo(
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        0L,
                        0
                );
                return new PostListAndPagingResponse(
                        board.getBoardName(),
                        List.of(),
                        emptyPage
                );
            }

            // 4) 기존 DB 조회 로직 그대로
            totalElements = postQueryRepository.countPosts(boardId, userId, idList);
            posts = postQueryRepository.searchAndOrderingPosts(
                    boardId,
                    userId,
                    sortBy,
                    direction,
                    pageable.getOffset(),
                    pageable.getPageSize(),
                    idList
            );
        } else {
            // 제목 검색이 없을 땐 기존 DB-only 로직
            totalElements = postQueryRepository.countPosts(boardId, null, userId);
            posts = postQueryRepository.searchAndOrderingPosts(
                    boardId,
                    null,
                    userId,
                    sortBy,
                    direction,
                    pageable.getOffset(),
                    pageable.getPageSize()
            );
        }

        // DTO → Response 변환
        List<PostListResponse> responseList = posts.stream()
                .map(dto -> new PostListResponse(
                        dto.id(),
                        dto.postTitle(),
                        dto.userId(),
                        dto.viewCount(),
                        dto.commentCount(),
                        dto.likeCount(),
                        dto.createdAt()
                ))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        PageInfo pageInfo = new PageInfo(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalElements,
                totalPages
        );

        return new PostListAndPagingResponse(
                board.getBoardName(),
                responseList,
                pageInfo
        );
    }

    //사용자가 좋아요 누른 게시글 목록 보기
    public PostsLikedAndPagingResponse likedPostList(String userId, Pageable pageable) {

        Member member = memberRepository.findByUserId(userId).orElseThrow(
                () -> new NoSuchElementException("유효하지 않은 사용자입니다.")
        );

        int pageNumber = pageable.getPageNumber() + 1;
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
