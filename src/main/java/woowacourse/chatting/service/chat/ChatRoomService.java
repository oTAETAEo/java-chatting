package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woowacourse.chatting.ChatRoomCache;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomType;
import woowacourse.chatting.repository.chat.ChatRoomRepository;
import woowacourse.chatting.service.MemberService;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCache chatRoomCache;

    private final MemberService memberService;

    public ChatRoom findChatRoom(String roomId) {
        return chatRoomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));
    }

    public ChatRoom getPublicChatRoom() {
        return chatRoomCache.getPublicRoom();
    }

    public ChatRoom createPrivateChatRoom(Member member1, Member member2) {
        return ChatRoom.builder()
                .id(UUID.randomUUID())
                .members(Set.of(member1, member2))
                .roomType(ChatRoomType.PRIVATE)
                .build();
    }

    public UUID findPrivateChatRoomIdByMemberEmail(String email1, String email2, ChatRoomType type){
        ChatRoom chatRoom = chatRoomRepository.findByUsers(email1, email2, type)
                .orElseGet(() -> savePrivateChatRoom(email1, email2));
        return chatRoom.getId();
    }

    private ChatRoom savePrivateChatRoom(String email1, String email2) {
        Member recipientMember = memberService.findByEmailMember(email1);
        Member senderMember = memberService.findByEmailMember(email2);
        ChatRoom privateChatRoom = createPrivateChatRoom(recipientMember, senderMember);
        chatRoomRepository.save(privateChatRoom);
        return privateChatRoom;
    }
}
