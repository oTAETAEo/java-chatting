package woowacourse.chatting.service;

import woowacourse.chatting.domain.Member;


public interface MemberService {

    Member findMember(Long memberId);

}
