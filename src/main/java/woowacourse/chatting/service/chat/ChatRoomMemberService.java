package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomMember;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.repository.chat.ChatRoomMemberRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository repository;

    // 방의 모든 멤버 가져오기
    public Set<Member> getMembers(ChatRoom chatRoom) {
        return repository.findAllByChatRoom(chatRoom).stream()
                .map(ChatRoomMember::getMember)
                .collect(Collectors.toSet());
    }
}