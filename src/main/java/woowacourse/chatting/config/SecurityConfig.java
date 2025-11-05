package woowacourse.chatting.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. 인가 설정: H2 콘솔 경로에 대해 permitAll() 적용
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll() // H2 콘솔 접근 허용
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )

                // 2. CSRF 비활성화 (H2 콘솔 경로에 대해서만)
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(PathRequest.toH2Console()) // H2 콘솔 경로 무시
                )

                // 3. X-Frame-Options 설정 (프레임 접근 허용)
                .headers((headers) -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        return http.build();
    }
}
