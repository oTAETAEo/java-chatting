package woowacourse.chatting.dto.chat;

import lombok.Builder;
import lombok.Data;
import woowacourse.chatting.domain.chat.PresenceStatus;

import java.util.UUID;

@Data
public class FriendDto {
    private UUID id;      // 친구의 ID
    private String name;  // 친구 이름
    private String email; // 친구 email
    private PresenceStatus status; // online - offline

    @Builder
    public FriendDto(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = PresenceStatus.OFFLINE;
    }

    public FriendDto() {
    }
}
