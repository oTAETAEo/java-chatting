package woowacourse.chatting.service.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.chat.ConnectFriendDto;
import woowacourse.chatting.dto.chat.FriendDto;
import woowacourse.chatting.repository.member.FriendRelationRepository;
import woowacourse.chatting.service.MemberService;
import woowacourse.chatting.util.UUIDUtil;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final FriendRelationRepository friendRelationRepository;
    private final MemberService memberService;
    private final ConnectedUserService connectedUserService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        UUID subId = UUIDUtil.toUUID(accessor.getUser().getName());
        Member member = memberService.findBySubId(subId);
        connectedUserService.addUser(sessionId, member);

        List<FriendDto> friends = friendRelationRepository.findFriendList(member.getId());
        for (FriendDto f: friends) {
            messagingTemplate.convertAndSendToUser(
                    f.getId().toString(),
                    "/queue/friend/connect",
                    new ConnectFriendDto(member.getSubId(), "online")
            );
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        UUID subId = UUIDUtil.toUUID(accessor.getUser().getName());
        Member member = memberService.findBySubId(subId);

        List<FriendDto> friends = friendRelationRepository.findFriendList(member.getId());
        for (FriendDto f: friends) {
            messagingTemplate.convertAndSendToUser(
                    f.getId().toString(),
                    "/queue/friend/connect",
                    new ConnectFriendDto(member.getSubId(), "offline")
            );
        }
        connectedUserService.removeUser(sessionId, member);
    }
}
