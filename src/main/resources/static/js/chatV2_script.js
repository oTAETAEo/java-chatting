// WebSocket 연결 정보
const WS_ENDPOINT = 'http://localhost:8080/ws/chat';

let stompClient = null;
let currentUserName = "Guest";
let currentChatPartner = null;
let currentChatRoomType = 'public';
let currentChatRoomId = null;

// --- 구독 ID 전역 관리 ---
let publicSub = null;
let privateSub = null;
let userListSub = null;
let historySub = null;
let publicRoomSub = null;

// ✅ 구독 초기화 플래그
let subscriptionsInitialized = false;

// --- JWT 토큰 디코딩 ---
function decodeJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
            '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        ).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("JWT 디코딩 실패:", e);
        return null;
    }
}

// --- 메시지 표시 ---
function displayMessage(user, message, type = 'public') {
    const box = document.getElementById('chat-messages');
    if (!box) return;

    if ((type === 'public' && currentChatRoomType !== 'public') ||
        (type === 'private' && currentChatRoomType !== 'private')) return;

    const msg = document.createElement('p');
    msg.classList.add('message');
    if (type === 'private') msg.classList.add('private-message');

    msg.innerHTML = `<strong>${user}:</strong> <span>${message}</span>`;
    box.appendChild(msg);
    box.scrollTop = box.scrollHeight;
}

// --- 사용자 목록 표시 ---
function renderUserList(users) {
    const container = document.getElementById('user-list');
    if (!container) return;

    container.innerHTML = '';

    // --- 공개 채널 버튼 ---
    const publicBtn = document.createElement('button');
    publicBtn.classList.add('user-item', 'public-chat');
    if (currentChatRoomType === 'public') publicBtn.classList.add('active');
    publicBtn.innerHTML = `<span class="status-dot" style="background-color: #7289da;"></span>공개 채널`;
    publicBtn.addEventListener('click', () => {
        startPublicChat();

        requestPublicHistory();
    });
    container.appendChild(publicBtn);

    // --- 개별 사용자 버튼 ---
    users.forEach(user => {
        if (user === currentUserName) return;

        const userBtn = document.createElement('button');
        userBtn.classList.add('user-item');
        userBtn.dataset.username = user;
        if (user === currentChatPartner) userBtn.classList.add('active');
        userBtn.innerHTML = `<span class="status-dot"></span>${user}`;
        userBtn.addEventListener('click', () => {
            startPrivateChat(user);
        });
        container.appendChild(userBtn);
    });
}

// --- 공개방 관련 ---
let publicRoomId = null;

function requestPublicHistory() {
    // if (!stompClient || !stompClient.connected) return;

    console.log("공개방 히스토리 메시지 가져오기")
    stompClient.send(`/app/history/public/${publicRoomId}`, {}, {});

    // if (!publicRoomId) {
    //     stompClient.send('/app/chat.getPublicRoom', {});
    // } else {
    //     console.log("공개방 히스토리 메시지 가져오기")
    //     stompClient.send(`/app/history/public/${publicRoomId}`, {}, {});
    // }
}

// --- 구독 설정 ---
function setupSubscriptions() {
    if (subscriptionsInitialized) return;

    console.log("📡 구독 초기화 중...");

    // --- 기존 구독 해제 ---
    publicSub?.unsubscribe();
    privateSub?.unsubscribe();
    userListSub?.unsubscribe();
    historySub?.unsubscribe();
    publicRoomSub?.unsubscribe();

    // --- 새 구독 설정 ---
    publicSub = stompClient.subscribe(`/topic/public/${currentChatRoomId}`, msg => {
        const message = JSON.parse(msg.body);
        displayMessage(message.sender, message.content);
    });

    privateSub = stompClient.subscribe("/user/queue/messages", payload => {
        const message = JSON.parse(payload.body);
        displayMessage(message.sender, message.content, 'private');
    });

    userListSub = stompClient.subscribe('/user/queue/users', msg => {
        const users = JSON.parse(msg.body);
        renderUserList(users);
    });

    publicRoomSub = stompClient.subscribe('/user/queue/public-room', msg => {
        const data = JSON.parse(msg.body);
        const newRoomId = data.roomId;

        if (newRoomId !== currentChatRoomId) {
            console.log("🔄 공개방 변경됨 → 재구독");
            publicSub?.unsubscribe();
            currentChatRoomId = newRoomId;
            publicSub = stompClient.subscribe(`/topic/public/${currentChatRoomId}`, msg => {
                const message = JSON.parse(msg.body);
                displayMessage(message.sender, message.content);
            });
        }
        publicRoomId = newRoomId;
        requestPublicHistory()
    });

    historySub = stompClient.subscribe('/user/queue/history', msg => {
        const messages = JSON.parse(msg.body);
        const chatBox = document.getElementById('chat-messages');
        chatBox.innerHTML = '';

        const type = (currentChatRoomType === 'public') ? 'public' : 'private';
        messages.forEach(data => displayMessage(data.sender, data.content, type));
    });

    subscriptionsInitialized = true;
}

// --- 채팅 전환 ---
function startPrivateChat(partner) {
    document.getElementById('chat-room-name').textContent = `1:1 대화: ${partner}`;
    document.getElementById('chat-messages').innerHTML = '';
    currentChatPartner = partner;
    currentChatRoomType = 'private';

    document.querySelectorAll('.user-item').forEach(i => i.classList.remove('active'));
    document.querySelector(`[data-username="${partner}"]`)?.classList.add('active');

    const token = localStorage.getItem('accessToken');
    if (!token) {
        alert("로그인이 필요합니다.");
        return;
    }

    fetch('/api/chat/private-room', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ recipientUsername: partner })
    })
        .then(response => {
            if (!response.ok) throw new Error('채팅방 요청 실패');
            return response.json();
        })
        .then(data => {
            const { roomId } = data;
            currentChatRoomId = roomId;
            subscribeToPrivateRoom(roomId);
        })
        .catch(err => {
            console.error(err);
            alert('채팅방 생성 중 오류가 발생했습니다.');
        });
}

// ✅ --- 특정 방 구독 및 히스토리 요청 ---
function subscribeToPrivateRoom(roomId) {
    if (!stompClient) return;

    // 기존 구독 해제
    if (window.currentSubscription) window.currentSubscription.unsubscribe();

    // 히스토리 요청
    stompClient.send(`/app/private/history/${roomId}`, {});

    // 방 구독
    window.currentSubscription = stompClient.subscribe(`/user/queue/private/${roomId}`, msg => {
        const data = JSON.parse(msg.body);

        // ✅ 배열로 히스토리 왔을 때 처리
        if (Array.isArray(data)) {
            const chatBox = document.getElementById('chat-messages');
            chatBox.innerHTML = '';
            data.forEach(m => displayMessage(m.sender, m.content, 'private'));
        } else {
            // 단일 메시지일 때
            displayMessage(data.sender, data.content, 'private');
        }
    });
}

// --- 공개 채팅 시작 ---
function startPublicChat() {
    if (currentChatRoomType === 'public') return;
    document.getElementById('chat-room-name').textContent = '채널: 모두와 대화 (공개 채팅)';
    document.getElementById('chat-messages').innerHTML = '';
    currentChatPartner = null;
    currentChatRoomType = 'public';
    document.querySelectorAll('.user-item').forEach(i => i.classList.remove('active'));
    document.querySelector('.public-chat')?.classList.add('active');
}

// --- 메시지 전송 ---
function sendMessage() {
    const input = document.getElementById('message-input');
    const message = input.value.trim();
    if (!message || !stompClient || !stompClient.connected) return;

    if (currentChatRoomType === 'public') {
        stompClient.send("/app/public", {}, JSON.stringify({ sender: currentUserName, content: message }));
    } else if (currentChatRoomType === 'private') {
        stompClient.send(`/app/private/${currentChatRoomId}`, {}, JSON.stringify({
            recipient: currentChatPartner,
            sender: currentUserName,
            content: message
        }));
        displayMessage(currentUserName, message, 'private');
    }

    input.value = '';
}

// --- WebSocket 연결 ---
function connectWebSocket() {
    const accessToken = localStorage.getItem('accessToken');
    const grantType = localStorage.getItem('grantType') || 'Bearer';
    const status = document.getElementById('status-message');
    const reconnectBtn = document.getElementById('reconnect-button');
    const sendBtn = document.getElementById('send-button');
    const disconnectBtn = document.getElementById('disconnect-button');

    subscriptionsInitialized = false;

    if (!accessToken) {
        status.textContent = '❌ 토큰이 없습니다. 로그인 필요.';
        reconnectBtn.disabled = true;
        return;
    }

    const decoded = decodeJwt(accessToken);
    if (decoded?.name) {
        currentUserName = decoded.name;
        const usernameInput = document.getElementById('username-input');
        usernameInput.value = currentUserName;
        usernameInput.disabled = true;
    }

    const socket = new SockJS(WS_ENDPOINT);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const headers = { 'Authorization': `${grantType} ${accessToken}` };

    stompClient.connect(headers, () => {
        status.textContent = `🟢 연결됨: ${currentUserName}`;
        sendBtn.disabled = false;
        disconnectBtn.disabled = false;
        reconnectBtn.disabled = true;

        setupSubscriptions();
        stompClient.send("/app/chat.getUsers", {}, JSON.stringify({}));
        stompClient.send('/app/chat.getPublicRoom', {});
        startPublicChat();
    }, error => {
        console.error("STOMP 연결 오류:", error);
        status.textContent = '🔴 STOMP 연결 오류';
        sendBtn.disabled = true;
        disconnectBtn.disabled = true;
        reconnectBtn.disabled = false;
    });
}

// --- 연결 종료 ---
function disconnectWebSocket() {
    const status = document.getElementById('status-message');
    if (stompClient && stompClient.connected) {
        publicSub?.unsubscribe();
        privateSub?.unsubscribe();
        userListSub?.unsubscribe();
        historySub?.unsubscribe();
        publicRoomSub?.unsubscribe();

        publicSub = privateSub = userListSub = historySub = publicRoomSub = null;
        subscriptionsInitialized = false;

        stompClient.disconnect(() => {
            status.textContent = '🟡 STOMP 연결 해제됨.';
            document.getElementById('send-button').disabled = true;
            document.getElementById('disconnect-button').disabled = true;
            document.getElementById('reconnect-button').disabled = false;
        });
    }
}

// --- 초기화 ---
window.onload = () => {
    connectWebSocket();
    document.getElementById('send-button').addEventListener('click', sendMessage);
    document.getElementById('message-input').addEventListener('keypress', e => {
        if (e.key === 'Enter') sendMessage();
    });
    document.getElementById('disconnect-button').addEventListener('click', disconnectWebSocket);
    document.getElementById('reconnect-button').addEventListener('click', connectWebSocket);
};
