package woowacourse.chatting.domain.chat;

import jakarta.persistence.*;
import lombok.*;
import woowacourse.chatting.domain.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;       // 어떤 채팅방의 메시지인지

    private String sender;       // 보낸 사람
    private String content;      // 메시지 본문

    @Enumerated(EnumType.STRING)
    private MessageType type;
}
