package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.ChatRoomCache;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.domain.chat.ChatMessage;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.MessageType;
import woowacourse.chatting.dto.chat.ChatMessageDto;
import woowacourse.chatting.dto.chat.PrivateMessageDto;
import woowacourse.chatting.dto.chat.RoomIdResponse;
import woowacourse.chatting.repository.chat.ChatMessageRepository;
import woowacourse.chatting.repository.chat.ChatRoomRepository;
import woowacourse.chatting.service.chat.ChatRoomService;
import woowacourse.chatting.service.chat.ChatService;
import woowacourse.chatting.service.webSocket.ConnectedUserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectedUserService connectedUserService;

    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    // 공개방. (소규모 Save로 처리 가능)소규모: 그냥 save()도 충분
    // 중규모: @Async + 배치 저장
    // 대규모: Redis 캐싱 → 배치 저장 + 파티션 DB
    @MessageMapping("/public")
    public void sendMessage(ChatMessageDto messageDto) {

        ChatRoom publicChatRoom = chatRoomService.getPublicChatRoom();
        chatService.chatMessageSave(publicChatRoom, messageDto);

        messagingTemplate.convertAndSend("/topic/public/" + publicChatRoom.getId(), messageDto);
    }

    @MessageMapping("/private/{roomId}")
    public void oneToOneMessage(ChatMessageDto message, @DestinationVariable String roomId) {

        ChatRoom chatRoom = chatRoomService.findChatRoom(roomId);
        chatService.chatMessageSave(chatRoom, message);

        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),             // 받는 사람
                "/queue/messages",                  // 구독 경로
                message                             // payload
        );
    }

    @MessageMapping("/chat.getPublicRoom")
    public void sendPublicRoomId(Principal principal) {

        ChatRoom publicChatRoom = chatRoomService.getPublicChatRoom();
        RoomIdResponse roomIdResponse = new RoomIdResponse(publicChatRoom.getId());

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/public-room",
                roomIdResponse
        );
    }

    @MessageMapping("/chat.getUsers")
    public void getUsers(Principal principal) {
        // 현재 접속자 목록 조회
        List<String> users = connectedUserService.getConnectedUsernames();

        // 요청한 사용자에게만 전송
        messagingTemplate.convertAndSendToUser(principal.getName(),
                "/queue/users",
                users);
    }

}
