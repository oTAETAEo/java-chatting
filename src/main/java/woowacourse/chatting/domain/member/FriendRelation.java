package woowacourse.chatting.domain.member;

import jakarta.persistence.*;
import lombok.*;
import woowacourse.chatting.domain.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRelation extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member")
    private Member from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member")
    private Member to;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;

    public void changeFriendStatus(FriendStatus status) {
        this.status = status;
    }
}

