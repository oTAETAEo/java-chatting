package woowacourse.chatting.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import woowacourse.chatting.exception.jwt.JwtValidationException;

@Component
@RequiredArgsConstructor
public class JwtStompInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP 연결 요청일 때만 토큰 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authorization 헤더에서 토큰 추출
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new JwtValidationException("토큰이 존재하지 않거나 Bearer 타입이 아닙니다.");
            }
            String token = authorizationHeader.substring(7);

            // 토큰 유효성 검증
            jwtTokenProvider.validateToken(token);

            // 인증 정보 설정
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            accessor.setUser(authentication);
        }

        return message;
    }
}
