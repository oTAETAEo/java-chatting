package woowacourse.chatting.service;

import woowacourse.chatting.domain.Member;
import woowacourse.chatting.dto.AddMemberRequest;


public interface MemberService {

    Member findMember(Long memberId);
    Long save(AddMemberRequest dto);
}
