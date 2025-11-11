package woowacourse.chatting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import woowacourse.chatting.domain.message.ChatRoom;
import woowacourse.chatting.domain.message.ChatRoomType;
import woowacourse.chatting.repository.message.ChatRoomRepository;

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
        UUID publicRoomId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // DB에 공개방이 있으면 조회, 없으면 생성
        publicRoom = chatRoomRepository.findById(publicRoomId)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder()
                                .id(publicRoomId)
                                .members(Set.of())
                                .type(ChatRoomType.GROUP)
                                .build()
                ));
    }
}
