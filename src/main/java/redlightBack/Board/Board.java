package redlightBack.Board;


import jakarta.persistence.*;
import lombok.*;
import redlightBack.common.BaseEntity;

@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String boardName;
    int postCount;

    public Board(String boardName) {
        this.boardName = boardName;
    }

}
