package woowacourse.chatting.dto.chat;

import lombok.*;

@Getter
@Setter
@Builder
public class ChatMessageDto {

    private String recipient;
    private String sender;
    private String content;
}
