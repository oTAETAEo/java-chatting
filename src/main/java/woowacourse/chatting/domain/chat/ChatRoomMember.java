package woowacourse.chatting.domain.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woowacourse.chatting.domain.BaseEntity;
import woowacourse.chatting.domain.member.Member;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMember extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ChatRoom chatRoom;

    @ManyToOne
    private Member member;

}
