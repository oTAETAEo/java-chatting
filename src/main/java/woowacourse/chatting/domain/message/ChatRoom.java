package woowacourse.chatting.domain.message;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

    @Id
    @Column(name = "chat_room_id", updatable = false, nullable = false)
    private UUID id;
}
