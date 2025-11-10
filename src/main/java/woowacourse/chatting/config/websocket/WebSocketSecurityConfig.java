package woowacourse.chatting.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import woowacourse.chatting.jwt.JwtStompInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtStompInterceptor jwtStompInterceptor;

    /**
     * 인바운드 STOMP 메시지(SEND, SUBSCRIBE 등)에 대한 인가 규칙을 설정합니다.
     * MessageMatcherDelegatingAuthorizationManager 빌더를 사용하여 람다 기반으로 규칙을 정의합니다.
     */
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {

        // 람다를 사용한 AuthorizationManager 구성
        return MessageMatcherDelegatingAuthorizationManager.builder()

                // 1. 프로토콜 관련 메시지 타입(CONNECT, HEARTBEAT 등)은 모두 허용합니다.
                //    (JWT 인증은 JwtStompInterceptor에서 이미 처리되므로 여기서 차단하지 않습니다.)
                .simpTypeMatchers(SimpMessageType.CONNECT,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.UNSUBSCRIBE)
                .permitAll()

                // 2. /app/* 목적지로 메시지를 보내는(SEND) 요청은 인증된 사용자만 허용합니다.
                .simpDestMatchers("/app/**").authenticated()

                // 3. /topic/*, /queue/* 목적지를 구독하는(SUBSCRIBE) 요청은 인증된 사용자만 허용합니다.
                .simpDestMatchers("/topic/**", "/queue/**").authenticated()

                // 4. 위에 명시되지 않은 모든 메시지는 거부하여 보안을 강화합니다.
                .anyMessage().denyAll()

                .build();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // jwtStompInterceptor를 STOMP 통신 채널의 인터셉터로 등록
        registration.interceptors(jwtStompInterceptor);
    }
}
