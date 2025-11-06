package woowacourse.chatting.jwt;

import lombok.Getter;

@Getter
public class AccessToken {

    private final String grantType;
    private final String token;

    public AccessToken(String token) {
        this.grantType = "Bearer";
        this.token = token;
    }
}
