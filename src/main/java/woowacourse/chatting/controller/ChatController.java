package woowacourse.chatting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.dto.ChatMessage;
import woowacourse.chatting.service.webSocket.ConnectedUserService;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    // SimpMessagingTemplate: STOMP 메시지 브로커를 통해 클라이언트에게 메시지를 전달하는 도구
    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectedUserService connectedUserService;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message, Principal principal) {
        // principal 객체는 StompHandler에서 JWT를 검증하고 설정한 인증된 사용자 정보입니다.
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof Member member) {

            // 메시지 객체에 인증된 사용자 이름 설정 (클라이언트가 보낸 sender 대신 서버에서 인증된 값 사용)
            String sender = member.getName();
            message.setSender(sender);
            log.info("메시지 수신: [보낸이: {}], [내용: {}]", sender, message.getContent());
        }

        // SimpMessagingTemplate을 사용하여 브로커의 /topic/chat 채널로 메시지를 전송합니다.
        // 이 메시지는 /topic/chat을 구독하는 모든 클라이언트에게 전달됩니다.
        String destination = "/topic/chat";
        messagingTemplate.convertAndSend(destination, message);

        log.info("메시지 전송 완료: [Destination: {}]", destination);
    }

    @MessageMapping("/chat.getUsers")
    public void getUsers() {
        // 현재 접속자 목록 조회
        List<String> users = connectedUserService.getConnectedUsernames();

        // 요청한 사용자에게만 전송
        messagingTemplate.convertAndSend("/topic/users", users);
    }

}
