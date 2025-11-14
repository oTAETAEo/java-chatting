package woowacourse.chatting.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.member.FriendRelation;

@Repository
public interface FriendRelationRepository extends JpaRepository<FriendRelation, Long> {

}
