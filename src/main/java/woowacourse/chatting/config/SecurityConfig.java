package woowacourse.chatting.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Spring Security의 Filter Chain을 설정하고 반환합니다.
     * 이 메서드는 H2 Console 사용을 위한 필수 설정과 기본 보안 정책을 정의합니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. 인가 설정: HTTP 요청에 대한 접근 권한 정의
                .authorizeHttpRequests((auth) -> auth
                        // H2 콘솔 경로 접근 허용 (인증 없이 permitAll)
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        // 그 외 모든 요청은 반드시 인증(로그인) 필요
                        .anyRequest().authenticated()
                )

                // 2. CSRF 비활성화 (H2 콘솔 경로에 대해서만)
                // H2 콘솔은 프레임과 자체 폼을 사용하여 CSRF 토큰을 올바르게 처리하지 못하기 때문에 무시합니다.
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(PathRequest.toH2Console())
                )

                // 3. X-Frame-Options 설정 (클릭재킹 방어)
                // 웹페이지가 다른 도메인의 프레임 안에 삽입되는 것을 방지합니다.
                .headers((headers) -> headers
                        // 같은 출처(Same Origin) 내의 프레임 삽입만 허용
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        return http.build();
    }
}
