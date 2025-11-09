package woowacourse.chatting.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import woowacourse.chatting.domain.Member;

import java.security.Principal; // 인증된 사용자 정보를 가져오기 위해 사용

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    // SimpMessagingTemplate: STOMP 메시지 브로커를 통해 클라이언트에게 메시지를 전달하는 도구
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message, Principal principal) {
        // principal 객체는 StompHandler에서 JWT를 검증하고 설정한 인증된 사용자 정보입니다.
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof Member member){

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

    // 이 클래스 내부에 사용할 메시지 DTO 정의
    public static class ChatMessage {
        private String sender;
        private String content;

        // Lombok을 사용하지 않을 경우 Getter/Setter/생성자를 수동으로 구현해야 합니다.
        // 예시를 위해 Getter와 Setter를 생략하고 간결하게 작성합니다.

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        // 기본 생성자 (JSON 역직렬화를 위해 필요)
        public ChatMessage() {}

        // 전체 생성자
        public ChatMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }
    }
}
