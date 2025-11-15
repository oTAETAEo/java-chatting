package woowacourse.chatting.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendDto {
    private Long id;      // 친구의 ID
    private String name;  // 친구 이름
    private String email; // 친구 email
}
