package woowacourse.chatting.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendResponseDto {
    private Long id;
    private String fromName;
    private String toName;
}
