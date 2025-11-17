package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.chat.ChatMessageDto;
import woowacourse.chatting.dto.chat.MessageResponse;
import woowacourse.chatting.dto.chat.RoomIdResponse;
import woowacourse.chatting.service.chat.ChatRoomMemberService;
import woowacourse.chatting.service.chat.ChatRoomService;
import woowacourse.chatting.service.chat.ChatService;
import woowacourse.chatting.service.webSocket.ConnectedUserService;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectedUserService connectedUserService;

    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomMemberService chatRoomMemberService;

    // 공개방. (소규모 Save로 처리 가능)소규모: 그냥 save()도 충분
    // 중규모: @Async + 배치 저장
    // 대규모: Redis 캐싱 → 배치 저장 + 파티션 DB
    @MessageMapping("/public")
    public void sendMessage(SimpMessageHeaderAccessor accessor, ChatMessageDto messageDto) {

        ChatRoom publicChatRoom = chatRoomService.getPublicChatRoom();

        String sessionId = accessor.getSessionId();
        Member member = connectedUserService.getMember(sessionId);
        chatService.chatMessageSave(publicChatRoom, messageDto, member);

        MessageResponse response = MessageResponse.builder()
                .sender(member.getName())
                .content(messageDto.getContent())
                .build();

        messagingTemplate.convertAndSend("/topic/public/" + publicChatRoom.getId(), response);
    }

    @MessageMapping("/private/{roomId}")
    public void oneToOneMessage(SimpMessageHeaderAccessor accessor, @DestinationVariable UUID roomId, ChatMessageDto messageDto) {

        ChatRoom chatRoom = chatRoomService.findChatRoom(roomId);

        String sessionId = accessor.getSessionId();
        Member member = connectedUserService.getMember(sessionId);

        chatService.chatMessageSave(chatRoom, messageDto, member);

        // recipient = 서버가 ChatRoomMember 조회 후 결정
        Member recipient = chatRoomMemberService.getMembers(chatRoom)
                .stream()
                .filter(m -> !Objects.equals(m.getId(), member.getId()))
                .findFirst()
                .orElseThrow();

        MessageResponse response = MessageResponse.builder()
                .sender(member.getName())
                .content(messageDto.getContent())
                .build();

        messagingTemplate.convertAndSendToUser(
                recipient.getSubId().toString(),
                "/queue/messages",
                response
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
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/users",
                users);
    }

}
