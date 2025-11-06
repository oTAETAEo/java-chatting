package woowacourse.chatting.service;

import woowacourse.chatting.jwt.JwtToken;

public interface MemberService {

    JwtToken singIn(String email, String password);

}
