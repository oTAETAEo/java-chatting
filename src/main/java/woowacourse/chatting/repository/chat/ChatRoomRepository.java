package woowacourse.chatting.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {


    @Query("""
            select r from ChatRoom r
                join r.members m
                where m.email in (:userA, :userB) and r.roomType = :type
                group by r
                having count(distinct m) = 2
            """)
    Optional<ChatRoom> findByUsers(@Param("userA") String userA, @Param("userB") String userB, @Param("type") ChatRoomType type);

}
