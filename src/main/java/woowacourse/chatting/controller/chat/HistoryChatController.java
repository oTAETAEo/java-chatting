package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.dto.chat.MessageResponse;
import woowacourse.chatting.repository.chat.ChatMessageRepository;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HistoryChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/history/public/{roomId}")
    public void getHistoryPublicMessage(@DestinationVariable UUID roomId, Principal principal) {

        List<MessageResponse> historyChatting = chatMessageRepository.findHistoryMessage(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/history",
                historyChatting
        );
    }

    @MessageMapping("/private/history/{roomId}")
    public void getHistoryPrivateMessage(@DestinationVariable UUID roomId, Principal principal) {

        List<MessageResponse> historyChatting = chatMessageRepository.findHistoryMessage(roomId);

        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/private/" + roomId,
                historyChatting
        );
    }
}
