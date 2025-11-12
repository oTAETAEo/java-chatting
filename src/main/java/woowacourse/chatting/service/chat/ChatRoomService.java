package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woowacourse.chatting.ChatRoomCache;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.repository.chat.ChatRoomRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCache chatRoomCache;

    public ChatRoom findChatRoom(String roomId){
        return chatRoomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 입니다.")
                );
    }

    public ChatRoom getPublicChatRoom(){
        return chatRoomCache.getPublicRoom();
    }


}
