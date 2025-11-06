package woowacourse.chatting.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.dto.ResponseDto;
import woowacourse.chatting.dto.ResponseToken;
import woowacourse.chatting.dto.SignInDto;
import woowacourse.chatting.jwt.JwtToken;
import woowacourse.chatting.service.MemberServiceImp;

@Slf4j
@RestController
@AllArgsConstructor
public class MemberController {

    private final MemberServiceImp memberServiceImp;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto> memberSignUp(@Validated @RequestBody AddMemberRequest dto) {
        memberServiceImp.save(dto);
        return ResponseEntity.ok(new ResponseDto("회원가입이 완료되었습니다"));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseToken> singIn(@RequestBody SignInDto signInDto, HttpServletResponse response) {
        JwtToken jwtToken = memberServiceImp.singIn(signInDto.getEmail(), signInDto.getPassword());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtToken.getRefreshToken())
                .httpOnly(true)       // JS 접근 불가
                .secure(false)         // HTTP 환경에서도 전송,
                .sameSite("Lax")      // CSRF 방지
                .path("/jwt/refresh") // 재발급 엔드포인트에만 전송되도록 경로 설정
                .maxAge(86400 * 30)   // 쿠키 만료 시간 (예: 30일)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                ResponseToken.builder()
                        .grantType(jwtToken.getGrantType())
                        .accessToken(jwtToken.getAccessToken())
                        .build()
        );
    }

    @GetMapping("/test")
    public String test() {
        return "success";
    }
}
