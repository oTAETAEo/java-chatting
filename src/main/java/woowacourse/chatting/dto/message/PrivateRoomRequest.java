package woowacourse.chatting.dto.message;

import lombok.Data;

@Data
public class PrivateRoomRequest{
    private String recipientUsername;
}