package woowacourse.chatting.service;

import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.AddMemberRequest;


public interface MemberService {

    Member findByEmailMember(String email);

    Member findMember(Long memberId);

    Long save(AddMemberRequest dto);

    FriendRelation addFriend(Member member, String friendEmail);
}
