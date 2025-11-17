package woowacourse.chatting.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woowacourse.chatting.domain.chat.ChatMessage;
import woowacourse.chatting.dto.chat.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select new woowacourse.chatting.dto.chat.MessageResponse(
            m.sender.name, m.content)
            from ChatMessage m where m.chatRoom.id = :roomId order by m.createdAt asc
            """)
    List<MessageResponse> findHistoryMessage(@Param("roomId") UUID chatRoomId);
}

