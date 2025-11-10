package woowacourse.chatting.service.webSocket;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectedUserService {

    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public void addUser(String username) {
        connectedUsers.add(username);
    }

    public void removeUser(String username) {
        connectedUsers.remove(username);
    }

    public List<String> getConnectedUsernames() {
        return new ArrayList<>(connectedUsers);
    }
}
