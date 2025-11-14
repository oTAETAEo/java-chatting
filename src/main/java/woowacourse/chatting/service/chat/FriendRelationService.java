package woowacourse.chatting.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.chat.FriendResponseDto;
import woowacourse.chatting.dto.chat.FriendsResponse;
import woowacourse.chatting.repository.member.FriendRelationRepository;
import woowacourse.chatting.service.MemberService;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendRelationService {

    private final MemberService memberService;
    private final FriendRelationRepository friendRelationRepository;

    public void friendRequest(FriendRelation friendRelation){
        friendRelationRepository.save(friendRelation);
    }

    public FriendsResponse getAllFriendRequest(Member member){

        Member findMember = memberService.findMember(member.getId());

        Set<FriendRelation> sentFriendRequests = findMember.getSentFriendRequests();
        Set<FriendRelation> receivedFriendRequests = findMember.getReceivedFriendRequests();

        List<FriendResponseDto> sent = sentFriendRequests.stream()
                .map(s -> FriendResponseDto.builder()
                        .fromName(s.getFrom().getName())
                        .toName(s.getTo().getName())
                        .id(s.getId())
                        .build())
                .toList();

        List<FriendResponseDto> received = receivedFriendRequests.stream()
                .map(s -> FriendResponseDto.builder()
                        .fromName(s.getFrom().getName())
                        .toName(s.getTo().getName())
                        .id(s.getId())
                        .build())
                .toList();
        return FriendsResponse.builder()
                .sentFriendRequests(sent)
                .receivedFriendRequests(received)
                .build();
    }
}
