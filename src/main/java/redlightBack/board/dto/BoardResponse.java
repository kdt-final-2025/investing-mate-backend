package redlightBack.board.dto;

public record BoardResponse(Long id,
                            String boardName,
                            int postCount
                            ) {
}
