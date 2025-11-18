package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.chat.PresenceStatus;
import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.domain.member.FriendStatus;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.chat.FriendDto;
import woowacourse.chatting.dto.chat.FriendResponseDto;
import woowacourse.chatting.dto.chat.FriendsResponse;
import woowacourse.chatting.repository.member.FriendRelationRepository;
import woowacourse.chatting.service.webSocket.ConnectedUserService;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FriendRelationService {

    private final ConnectedUserService connectedUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final FriendRelationRepository friendRelationRepository;

    public synchronized void friendRequest(FriendRelation friendRelation){
        friendRelationRepository.findFriendRelationBetween(
                friendRelation.getFrom().getId(), friendRelation.getTo().getId())
                .ifPresentOrElse((f) -> {
                    if (f.getStatus().equals(FriendStatus.REJECTED)){
                        f.changeFriendStatus(FriendStatus.REQUESTED);
                    }
                }, () -> friendRelationRepository.save(friendRelation));
    }

    public FriendsResponse getAllFriendRequest(Member member){

        List<FriendResponseDto> sent = friendRelationRepository.findAllSentFriendRequests(member.getId());
        List<FriendResponseDto> received = friendRelationRepository.findAllReceivedFriendRequests(member.getId());
        List<FriendDto> friends = friendRelationRepository.findFriendList(member.getId());
        friends
            .forEach((f) -> {
                f.setStatus(connectedUserService.containsFriend(f.getId()));
            });

        return FriendsResponse.builder()
                .sentFriendRequests(sent)
                .receivedFriendRequests(received)
                .friends(friends)
                .build();
    }

    public void friendStatusHandle(Member member, String status, Long id) {
        FriendRelation friendRelation = friendRelationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 친구추가 입니다."));

        if(!friendRelation.getTo().getId().equals(member.getId())){
            throw new IllegalArgumentException("권한 없음");
        }

        if (status.equalsIgnoreCase("ACCEPT")) {
            friendRelation.changeFriendStatus(FriendStatus.ACCEPTED);

            FriendDto build = FriendDto.builder()
                    .email(friendRelation.getTo().getEmail())
                    .id(friendRelation.getTo().getSubId())
                    .name(friendRelation.getTo().getName())
                    .build();
            build.setStatus(connectedUserService.containsFriend(member.getSubId()));

            messagingTemplate.convertAndSendToUser(
                    friendRelation.getFrom().getSubId().toString(),
                    "/queue/users",
                    build
            );
            return;
        }

        if (status.equalsIgnoreCase("REJECT")) {
            friendRelation.changeFriendStatus(FriendStatus.REJECTED);
            return;
        }

        throw new IllegalArgumentException("잘못된 action");
    }

}
