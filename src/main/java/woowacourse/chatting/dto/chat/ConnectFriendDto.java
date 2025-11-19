package woowacourse.chatting.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConnectFriendDto {
    private UUID subId;
    private String status;
}
