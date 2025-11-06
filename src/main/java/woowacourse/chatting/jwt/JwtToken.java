package woowacourse.chatting.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JwtToken {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;
}
