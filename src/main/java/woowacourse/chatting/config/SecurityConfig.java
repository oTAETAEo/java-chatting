package woowacourse.chatting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import woowacourse.chatting.jwt.JwtAuthenticationEntryPoint;
import woowacourse.chatting.jwt.filter.JwtAuthenticationFilter;
import woowacourse.chatting.jwt.JwtTokenProvider;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Spring Security의 Filter Chain을 설정하고 반환합니다.
     * 이 메서드는 H2 Console 사용을 위한 필수 설정과 stateless REST API를 위한 보안 정책을 정의합니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF(Cross-Site Request Forgery) 비활성화
                // REST API는 상태를 저장하지 않으므로(stateless), CSRF 공격에 대한 방어가 필요 없다.
                // 세션을 사용하지 않고, 각 요청마다 인증 정보를 보내기 때문이다.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 세션 관리 정책 설정
                // 세션을 사용하지 않고, 상태 없는(stateless) 인증을 사용하도록 설정한다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 인가 설정: HTTP 요청에 대한 접근 권한 정의
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/css/**",      // CSS 파일 허용
                                "/js/**",       // JavaScript 파일 허용
                                "/images/**",   // 이미지 파일 허용 (있다면)
                                "/*.html",      // 루트에 있는 HTML 파일 (예: index.html) 허용
                                "/favicon.ico" // 파비콘 허용
                        ).permitAll()

                        // H2 콘솔, 회원가입, 루트 경로는 인증 없이 접근 허용
                        .requestMatchers(
                                "/", "/sign-up", "/sign-in", "/jwt/refresh"
                        ).permitAll()

                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        // 그 외 모든 요청은 반드시 인증(로그인) 필요
                        .anyRequest().authenticated()
                )

                // 4. X-Frame-Options 설정 (클릭재킹 방어)
                // H2 콘솔은 프레임을 사용하므로, 같은 출처(Same Origin) 내의 프레임 삽입만 허용합니다.
                .headers((headers) -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

        ;

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/jwt/refresh");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}