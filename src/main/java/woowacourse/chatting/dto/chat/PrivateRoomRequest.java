package woowacourse.chatting.dto.chat;

import lombok.Data;

import java.util.UUID;

@Data
public class PrivateRoomRequest {
    private UUID recipientUsername;
}