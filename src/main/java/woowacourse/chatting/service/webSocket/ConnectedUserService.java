package woowacourse.chatting.service.webSocket;

import org.springframework.stereotype.Service;
import woowacourse.chatting.domain.member.Member;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectedUserService {

    // sessionId -> Member 매핑
    private final Map<String, Member> connectedUsers = new ConcurrentHashMap<>();

    public void addUser(String sessionId, Member member) {
        connectedUsers.put(sessionId, member);
    }

    public void removeUser(String sessionId) {
        connectedUsers.remove(sessionId);
    }

    public Member getMember(String sessionId) {
        return connectedUsers.get(sessionId);
    }

    public List<String> getConnectedUsernames() {
        return connectedUsers.values()
                .stream()
                .map(Member::getName)
                .toList();
    }
}
