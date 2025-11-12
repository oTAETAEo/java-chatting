package woowacourse.chatting.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import woowacourse.chatting.exception.jwt.JwtValidationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // HTTP 상태 코드를 401 Unauthorized로 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 응답 컨텐츠 타입을 JSON으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 응답 본문(JSON)에 담을 데이터 구성
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        // 🚨 JwtValidationException 정보 추출 (Filter에서 request.setAttribute로 예외를 담았다면 사용 가능)
        // Spring Security의 기본 Exception이 아니기 때문에, 직접 attribute를 확인합니다.
        Object exception = request.getAttribute("exception");

        if (exception instanceof JwtValidationException jwtException) {
            // 만약 토큰 만료 등의 상세 예외 정보가 있다면 상세 메시지를 반환
            errorDetails.put("error", "JWT Validation Failed");
            errorDetails.put("message", jwtException.getMessage());
        } else {
            // Spring Security 자체의 인증 실패 메시지를 사용
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "인증 정보가 유효하지 않습니다. 토큰을 확인하세요.");
        }

        // JSON 형태로 응답 스트림에 쓰기
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}
