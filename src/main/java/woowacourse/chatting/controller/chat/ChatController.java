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
import woowacourse.chatting.repository.chat.ChatMessageRepository;
import woowacourse.chatting.repository.chat.ChatRoomRepository;
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

    // SimpMessagingTemplate: STOMP 메시지 브로커를 통해 클라이언트에게 메시지를 전달하는 도구
    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectedUserService connectedUserService;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomCache chatRoomCache;

    // 공개방. (소규모 Save로 처리 가능)소규모: 그냥 save()도 충분
    // 중규모: @Async + 배치 저장
    // 대규모: Redis 캐싱 → 배치 저장 + 파티션 DB
    @MessageMapping("/public")
    public void sendMessage(ChatMessageDto messageDto, Principal principal) {

        ChatRoom publicRoom = chatRoomCache.getPublicRoom();

        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof Member member) {
            String sender = member.getName();
            messageDto.setSender(sender);

            ChatMessage message = ChatMessage.builder()
                    .chatRoom(publicRoom) // 공개방 설정
                    .content(messageDto.getContent())
                    .sender(sender)
                    .type(MessageType.TEXT)
                    .build();

            chatMessageRepository.save(message);
        }

        messagingTemplate.convertAndSend("/topic/public/" + publicRoom.getId(), messageDto);
    }


    @MessageMapping("/chat.getPublicRoom")
    public void sendPublicRoomId(SimpMessageHeaderAccessor headerAccessor) {
        ChatRoom publicRoom = chatRoomCache.getPublicRoom();
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomId", publicRoom.getId());

        messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/public-room",
                payload
        );
    }

    @MessageMapping("/private/{roomId}")
    public void oneToOneMessage(PrivateMessageDto message, @DestinationVariable String roomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(UUID.fromString(roomId)).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(message.getSender())
                .chatRoom(chatRoom)
                .content(message.getContent())
                .type(MessageType.PRIVATE)
                .build();
        chatMessageRepository.save(chatMessage);

        log.info("개인 메시지가 들어왔습니다. ");
        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),             // 받는 사람
                "/queue/messages",                  // 구독 경로
                message                             // payload
        );
    }

    @MessageMapping("/chat.getUsers")
    public void getUsers() {
        // 현재 접속자 목록 조회
        List<String> users = connectedUserService.getConnectedUsernames();

        // 요청한 사용자에게만 전송
        messagingTemplate.convertAndSend("/topic/users", users);
    }

}
