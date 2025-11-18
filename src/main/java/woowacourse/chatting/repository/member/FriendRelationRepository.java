package woowacourse.chatting.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.dto.chat.FriendDto;
import woowacourse.chatting.dto.chat.FriendResponseDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRelationRepository extends JpaRepository<FriendRelation, Long> {

    @Query("""
                select new woowacourse.chatting.dto.chat.FriendResponseDto(
                    f.id,
                    f.to.name,
                    f.to.email)
                from FriendRelation f where f.from.id = :fromMemberId and f.status = 'REQUESTED' order by f.createdAt asc
            """)
    List<FriendResponseDto> findAllSentFriendRequests(@Param("fromMemberId") Long fromMemberId);

    @Query("""
                select new woowacourse.chatting.dto.chat.FriendResponseDto(
                    f.id,
                    f.from.name,
                    f.from.email)
                from FriendRelation f where f.to.id = :toMemberId and f.status = 'REQUESTED' order by f.createdAt asc
            """)
    List<FriendResponseDto> findAllReceivedFriendRequests(@Param("toMemberId") Long toMemberId);

    @Query("""
                select new woowacourse.chatting.dto.chat.FriendDto(
                    case when f.from.id = :memberId then f.to.subId else f.from.subId end,
                    case when f.from.id = :memberId then f.to.name else f.from.name end,
                    case when f.from.id = :memberId then f.to.email else f.from.email end
                )
                from FriendRelation f
                where (f.from.id = :memberId or f.to.id = :memberId)
                  and f.status = 'ACCEPTED'
                order by f.createdAt asc
            """)
    List<FriendDto> findFriendList(@Param("memberId") Long memberId);

    @Query("select f from FriendRelation f where " +
            "(f.from.id = :id1 and f.to.id = :id2) or " +
            "(f.from.id = :id2 and f.to.id = :id1)")
    Optional<FriendRelation> findFriendRelationBetween(@Param("id1") Long id1, @Param("id2") Long id2);
}
