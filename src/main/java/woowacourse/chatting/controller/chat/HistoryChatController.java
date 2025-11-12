package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.dto.chat.ChatMessageDto;
import woowacourse.chatting.repository.chat.ChatMessageRepository;
import woowacourse.chatting.repository.chat.ChatRoomRepository;
import woowacourse.chatting.service.chat.HistoryChatService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HistoryChatController {

    private final HistoryChatService historyChatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomRepository chatRoomRepository;


    @MessageMapping("/history/public/{roomId}")
    public void getHistoryPublicMessage(@DestinationVariable String roomId, Principal principal){

        List<ChatMessageDto> historyChatting = historyChatService.findHistoryChatting(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/history",
                historyChatting
        );
    }

    @MessageMapping("/private/history/{roomId}")
    public void getHistoryPrivateMessage(@DestinationVariable String roomId, Principal principal) {

        List<ChatMessageDto> historyChatting = historyChatService.findHistoryChatting(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/private/" + roomId,
                historyChatting
        );
    }
}
