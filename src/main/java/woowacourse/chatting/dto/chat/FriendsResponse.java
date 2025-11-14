package woowacourse.chatting.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FriendsResponse {
    private List<FriendResponseDto> sentFriendRequests;
    private List<FriendResponseDto> receivedFriendRequests;
    private List<String> friends;
}
