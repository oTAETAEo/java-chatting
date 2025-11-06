package woowacourse.chatting.jwt;

import lombok.Getter;

@Getter
public class RefreshToken {
    private final String token;

    public RefreshToken(String token) {
        this.token = token;
    }
}
