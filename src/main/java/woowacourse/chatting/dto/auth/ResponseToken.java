package woowacourse.chatting.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ResponseToken {
    private final String grantType;
    private final String accessToken;
}
