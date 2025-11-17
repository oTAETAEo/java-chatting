package woowacourse.chatting.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FriendDto {
    private UUID id;      // 친구의 ID
    private String name;  // 친구 이름
    private String email; // 친구 email
}
