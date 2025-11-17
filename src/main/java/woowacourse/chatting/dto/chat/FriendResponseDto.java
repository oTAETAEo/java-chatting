package woowacourse.chatting.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FriendResponseDto {
    private Long id;
    private String toEmail;
    private String toName;
}
