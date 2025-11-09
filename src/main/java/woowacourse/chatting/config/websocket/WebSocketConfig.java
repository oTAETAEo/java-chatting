package woowacourse.chatting.config.websocket;

import ch.qos.logback.classic.pattern.MessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 메시지 처리를 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 클라이언트가 WebSocket 연결을 시작할 엔드포인트를 등록합니다.
     * SockJS를 사용하여 브라우저 호환성을 높입니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 이 경로로 접속하여 Handshake를 시작합니다.
        // Spring Security는 이 HTTP Handshake 요청을 가로채서 인증을 처리합니다.
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // CORS 설정 (* 대신 실제 도메인 권장)
                .withSockJS(); // SockJS 지원 활성화 (대부분의 브라우저에서 안정적인 연결을 위해 사용)
    }

    /**
     * 메시지 브로커(Message Broker)를 설정합니다.
     * 메시지를 어떤 Prefix로 라우팅할지 정의합니다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. /app/* 으로 시작하는 메시지는 @Controller의 @MessageMapping 메서드로 라우팅됩니다.
        config.setApplicationDestinationPrefixes("/app");

        // 2. /topic, /queue 로 시작하는 메시지는 브로커가 처리합니다. (클라이언트에게 전송)
        // /topic: 1:N 공통 메시징 (채팅방, 공지 등)
        // /queue: 1:1 개인 메시징
        config.enableSimpleBroker("/topic", "/queue");

        // 3. 사용자 고유의 queue 경로 Prefix 설정 (개인 메시지 전송 시 사용)
        // 예를 들어, /user/queue/messages 경로로 전송된 메시지는 인증된 사용자에게만 전달됩니다.
        config.setUserDestinationPrefix("/user");
    }
}