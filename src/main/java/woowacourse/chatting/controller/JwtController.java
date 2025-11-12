package woowacourse.chatting.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import woowacourse.chatting.dto.auth.ResponseToken;
import woowacourse.chatting.jwt.JwtTokenProvider;
import woowacourse.chatting.service.RefreshTokeService;

import static woowacourse.chatting.jwt.JwtRole.GRANT_TYPE;

@Slf4j
@RestController()
@RequiredArgsConstructor
public class JwtController {

    private final RefreshTokeService refreshTokeService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/jwt/refresh")
    public ResponseEntity<ResponseToken> reissueAccessToken(HttpServletRequest request) {

        Cookie refreshTokenCookie = refreshTokeService.findRefreshTokenCookieByName(request.getCookies());
        String refreshToken = refreshTokenCookie.getValue();

        String accessToken = jwtTokenProvider.reissueAccessToken(refreshToken);

        return ResponseEntity.ok(ResponseToken.builder()
                .grantType(GRANT_TYPE.getRole())
                .accessToken(accessToken)
                .build());
    }

}


