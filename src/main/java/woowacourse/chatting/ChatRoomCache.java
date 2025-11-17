package woowacourse.chatting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomType;
import woowacourse.chatting.repository.chat.ChatRoomRepository;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatRoomCache implements ApplicationRunner {

    private final ChatRoomRepository chatRoomRepository;

    @Getter
    public ChatRoom publicRoom;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ChatRoom publicChatRoom = ChatRoom.builder()
                .roomType(ChatRoomType.GROUP)
                .build();

        publicRoom = publicChatRoom;

        chatRoomRepository.save(publicChatRoom);
    }
}
