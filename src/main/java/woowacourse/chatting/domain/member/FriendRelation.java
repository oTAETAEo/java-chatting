package woowacourse.chatting.domain.member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRelation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_member")
    private Member from;

    @ManyToOne
    @JoinColumn(name = "to_member")
    private Member to;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;
}

