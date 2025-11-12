package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woowacourse.chatting.domain.chat.ChatMessage;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.MessageType;
import woowacourse.chatting.dto.chat.ChatMessageDto;
import woowacourse.chatting.repository.chat.ChatMessageRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public void chatMessageSave(ChatRoom chatRoom, ChatMessageDto messageDto) {
        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(chatRoom) // 공개방 설정
                .content(messageDto.getContent())
                .sender(messageDto.getSender())
                .type(MessageType.TEXT)
                .build());
    }

}
