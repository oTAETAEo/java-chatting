package woowacourse.chatting.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomMember;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 특정 채팅방의 모든 멤버 조회
    List<ChatRoomMember> findAllByChatRoom(ChatRoom chatRoom);
}
