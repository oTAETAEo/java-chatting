package woowacourse.chatting.service.webSocket;

import org.springframework.stereotype.Service;
import woowacourse.chatting.domain.chat.PresenceStatus;
import woowacourse.chatting.domain.member.Member;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectedUserService {

    // sessionId -> Member 매핑
    private final Map<String, Member> connectedUsers = new ConcurrentHashMap<>();
    private final Set<UUID> onlineUserIds = ConcurrentHashMap.newKeySet();

    public void addUser(String sessionId, Member member) {
        connectedUsers.put(sessionId, member);
        onlineUserIds.add(member.getSubId());
    }

    public void removeUser(String sessionId, Member member) {
        connectedUsers.remove(sessionId);
        onlineUserIds.remove(member.getSubId());
    }

    public Member getMember(String sessionId) {
        return connectedUsers.get(sessionId);
    }

    public PresenceStatus containsFriend(UUID subId) {
        if (onlineUserIds.contains(subId)) {
            return PresenceStatus.ONLINE;
        }
        return PresenceStatus.OFFLINE;
    }
}
