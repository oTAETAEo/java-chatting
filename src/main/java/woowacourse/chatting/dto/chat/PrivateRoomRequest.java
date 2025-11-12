package woowacourse.chatting.dto.chat;

import lombok.Data;

@Data
public class PrivateRoomRequest{
    private String recipientUsername;
}