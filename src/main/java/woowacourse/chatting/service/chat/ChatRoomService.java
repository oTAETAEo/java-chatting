package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.ChatRoomCache;
import woowacourse.chatting.domain.chat.ChatRoomMember;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomType;
import woowacourse.chatting.repository.chat.ChatRoomMemberRepository;
import woowacourse.chatting.repository.chat.ChatRoomRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCache chatRoomCache;

    public ChatRoom findChatRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다."));
    }

    public ChatRoom getPublicChatRoom() {
        return chatRoomCache.getPublicRoom();
    }

    @Transactional
    public UUID getOrCreatePrivateChatRoom(Member sender, Member recipient) {

        // 1. 기존 방 조회
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findPrivateRoomByMembers(sender, recipient);

        if (existingRoom.isPresent()) {
            return existingRoom.get().getId();
        }

        // 2. 새 방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomType(ChatRoomType.PRIVATE)
                .build();
        chatRoomRepository.save(chatRoom);

        // 3. ChatRoomMember 추가
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .build());

        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(recipient)
                .build());

        return chatRoom.getId();
    }
}
