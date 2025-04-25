package redlightBack.Board.Dto;

import java.time.LocalDateTime;

public record BoardResponse(Long id,
                            String boardName,
                            int postCount
                            ) {
}
