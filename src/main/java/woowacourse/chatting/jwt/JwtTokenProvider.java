package woowacourse.chatting.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.service.RefreshTokeService;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final UserDetailsService userDetailsService;
    private final RefreshTokeService refreshTokeService;

    /**
     * @param secretKey:<p> @Value("$jwt.secret")는 .yml에 저장 되어있는 key를 주입한다.
     * @param userDetailsService : <p>Spring Security의 표준 인터페이스를 통해 사용자 로드 기능을 주입받습니다.
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, UserDetailsService userDetailsService, RefreshTokeService refreshTokeService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.refreshTokeService = refreshTokeService;
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
    }

    /**
     * 로그인이 끝난 후 실행되기 때문에 캐스팅에 문제가 발생할수 없다.
     * <p>
     * 로그인이 끝났다는건 앞단에서 DB에 있는 데이터와 일치한걸 확인한 경우이다.
     */
    public JwtToken generateToken(Authentication authentication){

        // Authentication 객체에서 Member 정보 추출
        Member member = (Member) authentication.getPrincipal();

        // 권한 가져오기.
        String authorities = getAuthorities(authentication);

        // Access Token 생성
        String accessToken = getAccessToken(member, authorities);

        // Refresh Token 생성
        String refreshToken = getRefreshToken(member);
        refreshTokeService.save(member.getId(), refreshToken);

        return JwtToken.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String accessToken){

        // 1. Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if(claims.get("auth") == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 2. 토큰의 Subject 이메일 추출
        String username = claims.getSubject();

        // 3. UserDetailsService를 통해 DB/캐시에서 실제 Member 객체를 로드 (Principal)
        UserDetails principal = userDetailsService.loadUserByUsername(username);

        // 4. Member 객체의 권한 목록을 사용하여 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token or Signature Mismatch", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Member 객체는 UserDetails를 구현하고 있다. 이때 getAuthority() 메서드도 구현 하기 때문에 다형성으로 권한을 얻어올수 있다.
     * @param authentication
     * @return string : authority
     */
    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private String getAccessToken(Member member, String authorities) {
        return Jwts.builder()
                .subject(member.getEmail())
                .claim("auth", authorities)
                .claim("memberId", member.getId())
                .claim("name", member.getName())
                .expiration(new Date(getNow() + 86400000))
                .signWith(key)
                .compact();
    }

    private String getRefreshToken(Member member) {
        return Jwts.builder()
                .expiration(new Date(getNow() + 86400000))
                .subject(member.getEmail()) // Refresh Token의 Subject로 Email 사용
                .signWith(key)
                .compact();
    }

    private long getNow() {
        return new Date().getTime();
    }
}
