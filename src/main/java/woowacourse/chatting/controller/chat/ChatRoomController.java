package woowacourse.chatting.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.domain.chat.ChatRoomType;
import woowacourse.chatting.dto.chat.PrivateRoomRequest;
import woowacourse.chatting.dto.chat.RoomIdResponse;
import woowacourse.chatting.service.MemberService;
import woowacourse.chatting.service.chat.ChatRoomService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    @PostMapping("/api/chat/private-room")
    public ResponseEntity<RoomIdResponse> getPrivateChatRoomId(
            @RequestBody PrivateRoomRequest roomRequest,
            @AuthenticationPrincipal UUID sender) {

        Member recipient = memberService.findBySubId(roomRequest.getRecipientUsername());
        Member member = memberService.findBySubId(sender);

        // 방 조회 또는 생성
        UUID chatRoomId = chatRoomService.getOrCreatePrivateChatRoom(member, recipient);

        return ResponseEntity.ok(new RoomIdResponse(chatRoomId));
    }
}

