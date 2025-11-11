package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.domain.message.ChatRoom;
import woowacourse.chatting.dto.message.ChatMessageDto;
import woowacourse.chatting.repository.message.ChatMessageRepository;
import woowacourse.chatting.repository.message.ChatRoomRepository;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HistoryChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @MessageMapping("/history/public/{roomId}")
    public void getHistoryPublicMessage(@DestinationVariable String roomId, Principal principal){

        List<ChatMessageDto> messages = chatMessageRepository.findHistoryMessage(UUID.fromString(roomId))
                .stream()
                .map(m -> new ChatMessageDto(m.getSender(), m.getContent()))
                .toList();

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/history",
                messages
        );
    }

    @MessageMapping("/private/history/{roomId}")
    public void getHistoryPrivateMessage(@DestinationVariable String roomId, Principal principal) {

        ChatRoom chatRoom = chatRoomRepository.findById(UUID.fromString(roomId)).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        List<ChatMessageDto> historyMessageDto = chatMessageRepository.findHistoryMessage(chatRoom.getId())
                .stream()
                .map(m -> new ChatMessageDto(m.getSender(), m.getContent()))
                .toList();

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/private/" + roomId,
                historyMessageDto
        );
    }
}
