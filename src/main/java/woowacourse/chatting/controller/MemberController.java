package woowacourse.chatting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.AddFriendDto;
import woowacourse.chatting.dto.ResponseDto;
import woowacourse.chatting.dto.chat.FriendRequestStatusDto;
import woowacourse.chatting.dto.chat.FriendsResponse;
import woowacourse.chatting.service.MemberService;
import woowacourse.chatting.service.chat.FriendRelationService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final FriendRelationService friendRelationService;

    @PostMapping("/add/friend")
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal Member member, @RequestBody AddFriendDto friendDto) {
        FriendRelation friendRelation = memberService.addFriend(member, friendDto.getFriendEmail());
        friendRelationService.friendRequest(friendRelation);
        return ResponseEntity.ok().body(new ResponseDto("친구요청 완료."));
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriendRequest(@AuthenticationPrincipal Member member){
        FriendsResponse allFriendRequest = friendRelationService.getAllFriendRequest(member);
        return ResponseEntity.ok()
                .body(allFriendRequest);
    }

    @PatchMapping("/friend/status/{id}")
    public ResponseEntity<?> friendRequestSuccess(
            @RequestBody FriendRequestStatusDto statusDto,
            @PathVariable Long id,
            @AuthenticationPrincipal Member member) {

        friendRelationService.friendStatusHandle(member, statusDto.getStatus(), id);

        return ResponseEntity.ok().build();
    }
}
