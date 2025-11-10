// WebSocket 연결 정보
const WS_ENDPOINT = 'http://localhost:8080/ws/chat';

let stompClient = null;
let currentUserName = "Guest";
let currentChatPartner = null;
let currentChatRoomType = 'public';

// 구독 ID 전역 관리
let publicSub = null;
let privateSub = null;
let userListSub = null;

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

    // 현재 채팅방과 메시지 타입 확인
    if ((type === 'public' && currentChatRoomType !== 'public') ||
        (type === 'private' && currentChatRoomType !== 'private')) return;

    const msg = document.createElement('p');
    msg.classList.add('message');
    if (type === 'private') msg.classList.add('private-message');

    msg.innerHTML = `<strong>${user}:</strong> <span>${message}</span>`;
    box.appendChild(msg);
    box.scrollTop = box.scrollHeight;
}

// --- 사용자 목록 렌더링 ---
function renderUserList(users) {
    const container = document.getElementById('user-list');
    if (!container) return;

    container.innerHTML = '';

    // 공개 채널
    const publicRoom = document.createElement('div');
    publicRoom.classList.add('user-item', 'public-chat');
    if (currentChatRoomType === 'public') publicRoom.classList.add('active');
    publicRoom.innerHTML = `<span class="status-dot" style="background-color: #7289da;"></span>공개 채널`;
    publicRoom.addEventListener('click', startPublicChat);
    container.appendChild(publicRoom);

    users.forEach(user => {
        if (user === currentUserName) return;

        const userItem = document.createElement('div');
        userItem.classList.add('user-item');
        userItem.dataset.username = user;
        if (user === currentChatPartner) userItem.classList.add('active');
        userItem.innerHTML = `<span class="status-dot"></span>${user}`;
        userItem.addEventListener('click', () => startPrivateChat(user));
        container.appendChild(userItem);
    });
}

// --- 1:1 채팅 시작 ---
function startPrivateChat(partner) {
    document.getElementById('chat-room-name').textContent = `1:1 대화: ${partner}`;
    document.getElementById('chat-messages').innerHTML = '';
    currentChatPartner = partner;
    currentChatRoomType = 'private';

    document.querySelectorAll('.user-item').forEach(i => i.classList.remove('active'));
    document.querySelector(`[data-username="${partner}"]`)?.classList.add('active');
}

// --- 공개 채팅 시작 ---
function startPublicChat() {
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

    const payload = {
        sender: currentUserName,
        content: message,
        recipient: currentChatRoomType === 'private' ? currentChatPartner : null
    };

    const dest = currentChatRoomType === 'public'
        ? "/app/chat"
        : "/app/chat.sendPrivateMessage";

    stompClient.send(dest, {}, JSON.stringify(payload));

    if (currentChatRoomType === 'private') {
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

    if (stompClient && stompClient.connected) {
        publicSub?.unsubscribe();
        privateSub?.unsubscribe();
        userListSub?.unsubscribe();
        stompClient.disconnect(() => console.log("🔸 기존 STOMP 연결 해제 완료"));
    }

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
        console.log('🟢 STOMP 연결 성공');
        status.textContent = `🟢 연결됨: ${currentUserName}`;
        sendBtn.disabled = false;
        disconnectBtn.disabled = false;
        reconnectBtn.disabled = true;

        // 공개 채팅 구독
        publicSub = stompClient.subscribe('/topic/chat', msg => {
            const data = JSON.parse(msg.body);
            if (currentChatRoomType === 'public') {
                displayMessage(data.sender || '시스템', data.content);
            }
        });

        // 개인 메시지 구독
        privateSub = stompClient.subscribe('/user/queue/messages', msg => {
            const data = JSON.parse(msg.body);
            if (currentChatRoomType === 'private' && (data.sender === currentChatPartner || data.recipient === currentChatPartner)) {
                displayMessage(data.sender || '시스템', data.content, 'private');
            }
        });

        // 사용자 목록 구독
        userListSub = stompClient.subscribe('/topic/users', msg => {
            const users = JSON.parse(msg.body);
            renderUserList(users);
        });

        // 사용자 목록 초기 요청
        stompClient.send("/app/chat.getUsers", {}, JSON.stringify({}));

        startPublicChat();
    }, (error) => {
        console.error("STOMP 연결 오류:", error);
        status.textContent = '🔴 STOMP 연결 오류. 재연결 버튼을 눌러주세요.';
        sendBtn.disabled = true;
        disconnectBtn.disabled = true;
        reconnectBtn.disabled = false;
    });
}

// --- 연결 종료 ---
function disconnectWebSocket() {
    const status = document.getElementById('status-message');
    const sendBtn = document.getElementById('send-button');
    const disconnectBtn = document.getElementById('disconnect-button');
    const reconnectBtn = document.getElementById('reconnect-button');

    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            console.log("🟡 연결 해제됨");
            status.textContent = '🟡 STOMP 연결 해제됨.';
            sendBtn.disabled = true;
            disconnectBtn.disabled = true;
            reconnectBtn.disabled = false;
        });
    }
}

// --- 로그아웃 ---
function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('grantType');
    window.location.href = 'index.html';
}

// --- 테스트 API ---
async function sendTestApiRequest() {
    console.log("[API 테스트 요청]");
}

// --- 초기화 ---
window.onload = () => {
    connectWebSocket();

    document.getElementById('send-button').addEventListener('click', sendMessage);
    document.getElementById('message-input').addEventListener('keypress', e => {
        if (e.key === 'Enter') sendMessage();
    });
    document.getElementById('disconnect-button').addEventListener('click', disconnectWebSocket);
    document.getElementById('logout-button').addEventListener('click', logout);
    document.getElementById('test-api-button').addEventListener('click', sendTestApiRequest);
    document.getElementById('reconnect-button').addEventListener('click', () => {
        if (stompClient && stompClient.connected) {
            alert("이미 연결되어 있습니다.");
            return;
        }
        document.getElementById('status-message').textContent = '🔄 재연결 중...';
        connectWebSocket();
    });
};
