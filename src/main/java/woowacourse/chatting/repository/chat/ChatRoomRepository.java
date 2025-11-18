package woowacourse.chatting.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.member.Member;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("""
                select r from ChatRoom r
                join ChatRoomMember m1 on m1.chatRoom = r
                join ChatRoomMember m2 on m2.chatRoom = r
                where r.roomType = 'PRIVATE'
                  and m1.member = :member1
                  and m2.member = :member2
            """)
    Optional<ChatRoom> findPrivateRoomByMembers(Member member1, Member member2);

}
