package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woowacourse.chatting.dto.chat.ChatMessageDto;
import woowacourse.chatting.repository.chat.ChatMessageRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryChatService {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessageDto> findHistoryChatting(String roomId) {
        return chatMessageRepository.findHistoryMessage(UUID.fromString(roomId))
                .stream()
                .map(m -> ChatMessageDto.builder()
                        .sender(m.getSender())
                        .content(m.getContent())
                        .build()
                )
                .toList();
    }

}
