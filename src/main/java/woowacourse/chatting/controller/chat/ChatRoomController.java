package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.domain.chat.ChatRoom;
import woowacourse.chatting.domain.chat.ChatRoomType;
import woowacourse.chatting.dto.chat.PrivateRoomRequest;
import woowacourse.chatting.dto.chat.PrivateRoomResponse;
import woowacourse.chatting.repository.chat.ChatRoomRepository;
import woowacourse.chatting.service.MemberService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;

    @PostMapping("/api/chat/private-room")
    public ResponseEntity<?> getPrivateChatRoomId(@RequestBody PrivateRoomRequest roomRequest, @AuthenticationPrincipal Member member){
        Optional<ChatRoom> findChatRoom = chatRoomRepository.findByUsers(member.getEmail(), roomRequest.getRecipientUsername(), ChatRoomType.PRIVATE);

        if (findChatRoom.isPresent()){
            return ResponseEntity.ok(new PrivateRoomResponse(findChatRoom.get().getId()));
        }

        Member recipientMember = memberService.findByEmailMember(roomRequest.getRecipientUsername());
        Member senderMember = memberService.findMember(member.getId());
        ChatRoom createChatRoom = new ChatRoom(UUID.randomUUID(), Set.of(recipientMember, senderMember), ChatRoomType.PRIVATE);
        chatRoomRepository.save(createChatRoom);

        return ResponseEntity.ok(new PrivateRoomResponse(createChatRoom.getId()));
    }
}

