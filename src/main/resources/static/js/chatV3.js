// WebSocket 연결 정보
const WS_ENDPOINT = 'http://localhost:8080/ws/chat';

let stompClient = null;
let currentUserName = "Guest";
let currentChatPartner = null;
let currentChatRoomType = 'public';
let currentChatRoomId = null;
let currentUserEmail = null; // Add this line
let allFriends = []; // Global variable to store friends

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

    const msg = document.createElement('div');
    msg.classList.add('message');

    if (user === currentUserName) {
        msg.classList.add('my-message');
    }

    if (type === 'private') msg.classList.add('private-message');

    const sender = document.createElement('strong');
    sender.textContent = user;

    const content = document.createElement('span');
    content.textContent = message;

    msg.appendChild(sender);
    msg.appendChild(content);

    box.appendChild(msg);
    box.scrollTop = box.scrollHeight;
}

// --- 사용자 프로필 업데이트 ---
function updateUserProfile(name, email) {
    document.getElementById('profile-name').textContent = name;
    document.getElementById('profile-email').textContent = email;
}

// --- 사용자 목록 표시 ---
function renderUserList(onlineUsers, allFriends) {
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

    // --- 친구 목록 ---
    allFriends.forEach(friend => {
        // Check if the friend is currently online
        const isOnline = onlineUsers.includes(friend.email);

        const userBtn = document.createElement('button');
        userBtn.classList.add('user-item');
        userBtn.dataset.username = friend.email;
        if (friend.email === currentChatPartner) userBtn.classList.add('active');

        const statusDotColor = isOnline ? '#43b581' : '#747474'; // Green for online, gray for offline
        userBtn.innerHTML = `<span class="status-dot" style="background-color: ${statusDotColor};"></span>${friend.name}`;
        userBtn.addEventListener('click', () => {
            startPrivateChat(friend.id, friend.name);
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
        const onlineUsers = JSON.parse(msg.body);
        renderUserList(onlineUsers, allFriends);
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
function startPrivateChat(partnerId, partnerName) {
    document.getElementById('chat-room-name').textContent = `1:1 대화: ${partnerName}`;
    document.getElementById('chat-messages').innerHTML = '';
    currentChatPartner = partnerId;
    currentChatRoomType = 'private';

    document.querySelectorAll('.user-item').forEach(i => i.classList.remove('active'));
    document.querySelector(`[data-username="${partnerId}"]`)?.classList.add('active');

    const token = sessionStorage.getItem('accessToken');
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
        body: JSON.stringify({ recipientUsername: partnerId })
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
    const accessToken = sessionStorage.getItem('accessToken');
    const grantType = sessionStorage.getItem('grantType') || 'Bearer';
    const status = document.getElementById('status-message');
    const reconnectBtn = document.getElementById('reconnect-button');
    const sendBtn = document.getElementById('send-button');
    const disconnectBtn = document.getElementById('disconnect-button');

    subscriptionsInitialized = false;

    if (!accessToken) {
        status.style.backgroundColor = 'red';
        reconnectBtn.disabled = true;
        return;
    }

    const decoded = decodeJwt(accessToken);
    if (decoded?.username) { // Changed from decoded?.name to decoded?.username
        currentUserName = decoded.username;
        currentUserEmail = decoded.email; // Changed from decoded.sub to decoded.email
        updateUserProfile(currentUserName, currentUserEmail);
        const usernameInput = document.getElementById('username-input');
        usernameInput.value = currentUserName;
        usernameInput.disabled = true;
    }

    const socket = new SockJS(WS_ENDPOINT);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const headers = { 'Authorization': `${grantType} ${accessToken}` };

    stompClient.connect(headers, async () => { // Added async here
        status.style.backgroundColor = 'green';
        sendBtn.disabled = false;
        disconnectBtn.disabled = false;
        reconnectBtn.disabled = true;

        await fetchFriendData(); // Fetch friends initially
        setupSubscriptions();
        stompClient.send("/app/chat.getUsers", {}, JSON.stringify({}));
        stompClient.send('/app/chat.getPublicRoom', {});
        startPublicChat();
    }, error => {
        console.error("STOMP 연결 오류:", error);
        status.style.backgroundColor = 'red';
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
            status.style.backgroundColor = 'gray';
            document.getElementById('send-button').disabled = true;
            document.getElementById('disconnect-button').disabled = true;
            document.getElementById('reconnect-button').disabled = false;
        });
    }
}

// --- 로그아웃 ---
async function logout() {
    const accessToken = sessionStorage.getItem('accessToken');
    const grantType = sessionStorage.getItem('grantType') || 'Bearer';

    try {
        await fetch('/logout', {
            method: 'POST',
            headers: {
                'Authorization': `${grantType} ${accessToken}`
            }
        });
    } catch (error) {
        console.error('Logout API call failed:', error);
    } finally {
        disconnectWebSocket();
        sessionStorage.removeItem('accessToken');
        sessionStorage.removeItem('grantType');
        window.location.href = '/index.html';
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
    document.getElementById('logout-button').addEventListener('click', logout);

    document.getElementById('add-friend-button').addEventListener('click', () => {
        const email = prompt('추가할 친구의 이메일을 입력하세요.');
        if (email) {
            addFriend(email);
        }
    });

    const userProfile = document.getElementById('user-profile');
    const profileModal = document.getElementById('profile-modal');
    const modalContainer = document.getElementById('modal-container');

    userProfile.addEventListener('click', () => {
        profileModal.classList.toggle('modal-visible');
    });

    modalContainer.addEventListener('click', (e) => {
        if (e.target === modalContainer) {
            profileModal.classList.remove('modal-visible');
        }
    });

    const friendManagementButton = document.getElementById('friend-management-button');
    const friendManagementView = document.getElementById('friend-management-view');
    const closeFriendViewButton = document.getElementById('close-friend-view-button');
    const friendManagementBackdrop = document.getElementById('friend-management-backdrop'); // Get the backdrop element
    const tabs = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    friendManagementButton.addEventListener('click', async () => {
        friendManagementView.classList.remove('hidden');
        friendManagementBackdrop.classList.remove('hidden'); // Show the backdrop
        profileModal.classList.remove('modal-visible'); // Close the profile modal

        // Fetch friend data
        await fetchFriendData();
    });

    closeFriendViewButton.addEventListener('click', () => {
        friendManagementView.classList.add('hidden');
        friendManagementBackdrop.classList.add('hidden'); // Hide the backdrop
    });

    friendManagementBackdrop.addEventListener('click', () => {
        friendManagementView.classList.add('hidden');
        friendManagementBackdrop.classList.add('hidden');
    });

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            const target = tab.getAttribute('data-tab');
            tabContents.forEach(content => {
                content.classList.remove('active');
                if (content.id === `${target}-content`) {
                    content.classList.add('active');
                }
            });
        });
    });
};

// --- 친구 데이터 가져오기 및 표시 ---
async function fetchWithAuth(url, options = {}) {
    let response = await fetch(url, options);

    if (response.status === 401) {
        console.log("[Auth] 401 Unauthorized. 토큰 재발급을 시도합니다.");

        try {
            const refreshResponse = await fetch('/jwt/refresh', { method: 'GET' });
            if (!refreshResponse.ok) { throw new Error('토큰 재발급에 실패했습니다.'); }

            const tokenData = await refreshResponse.json();
            const newAccessToken = tokenData.accessToken;
            const newGrantType = tokenData.grantType;

            sessionStorage.setItem('accessToken', newAccessToken);
            sessionStorage.setItem('grantType', newGrantType);
            console.log("[Auth] 새로운 액세스 토큰을 발급받아 저장했습니다.");

            const newOptions = { ...options };
            newOptions.headers = { ...options.headers };
            newOptions.headers['Authorization'] = `${newGrantType} ${newAccessToken}`;

            console.log("[Auth] 새로운 토큰으로 원래 요청을 재시도합니다.");
            response = await fetch(url, newOptions);

        } catch (error) {
            console.error("[Auth] 토큰 재발급 과정에서 오류가 발생했습니다:", error);
            window.location.href = '/index.html'; // 예시: 로그인 페이지로 이동
            return; // 추가 진행 중단
        }
    }

    return response;
}

async function fetchFriendData() {
    try {
        const response = await fetchWithAuth('/friends', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            }
        });

        if (!response.ok) {
            throw new Error('친구 데이터를 가져오는데 실패했습니다.');
        }

        const data = await response.json();
        console.log("친구 데이터:", data);
        allFriends = data.friends || []; // Update global allFriends variable

        // 보낸 요청 표시
        const sentRequestsContent = document.getElementById('sent-requests-content');
        sentRequestsContent.innerHTML = '';
        if (data.sentFriendRequests && data.sentFriendRequests.length > 0) {
            data.sentFriendRequests.forEach(request => {
                const item = createFriendItem(request, 'sent', request.id); // Pass the whole request object
                sentRequestsContent.appendChild(item);
            });
        } else {
            sentRequestsContent.innerHTML = '<div class="no-content-message">보낸 친구 요청이 없습니다.</div>';
        }

        // 받은 요청 표시
        const receivedRequestsContent = document.getElementById('received-requests-content');
        receivedRequestsContent.innerHTML = '';
        if (data.receivedFriendRequests && data.receivedFriendRequests.length > 0) {
            data.receivedFriendRequests.forEach(request => {
                const item = createFriendItem(request, 'received', request.id); // Pass the whole request object
                receivedRequestsContent.appendChild(item);
            });
        } else {
            receivedRequestsContent.innerHTML = '<div class="no-content-message">받은 친구 요청이 없습니다.</div>';
        }

        // 모든 친구 표시
        const allFriendsContent = document.getElementById('all-friends-content');
        allFriendsContent.innerHTML = '';
        if (data.friends && data.friends.length > 0) {
            data.friends.forEach(friend => {
                const item = createFriendItem(friend, 'friend');
                allFriendsContent.appendChild(item);
            });
        } else {
            allFriendsContent.innerHTML = '<div class="no-content-message">친구가 없습니다.</div>';
        }

    } catch (error) {
        console.error('친구 데이터 로딩 중 오류:', error);
        alert('친구 데이터를 가져오는데 실패했습니다.');
    }
}

// --- 친구 목록 아이템 생성 ---
function createFriendItem(data, type, id = null) { // 'data' can be string (name) or object ({name, email} or {toName, toEmail} or {fromName, fromEmail})
    const item = document.createElement('div');
    item.className = 'friend-item';

    const pic = document.createElement('div');
    pic.className = 'friend-item-pic';

    const actions = document.createElement('div');
    actions.className = 'friend-item-actions';

    item.appendChild(pic);

    if (type === 'friend') {
        // data is a friend object: { id, name, email }
        const info = document.createElement('div');
        info.className = 'friend-item-info';

        const itemName = document.createElement('div');
        itemName.className = 'friend-item-name';
        itemName.textContent = data.name;

        const itemEmail = document.createElement('div');
        itemEmail.className = 'friend-item-email';
        itemEmail.textContent = data.email;

        info.appendChild(itemName);
        info.appendChild(itemEmail);
        item.appendChild(info);

        const removeButton = document.createElement('button');
        removeButton.className = 'remove-btn';
        removeButton.textContent = '삭제';
        removeButton.onclick = () => removeFriend(data.name);
        actions.appendChild(removeButton);

        const blockButton = document.createElement('button');
        blockButton.className = 'block-btn';
        blockButton.textContent = '차단';
        blockButton.onclick = () => {
            if (confirm(`${data.name}님을 정말 차단하시겠습니까?`)) {
                blockFriend(data.name);
            }
        };
        actions.appendChild(blockButton);
    } else if (type === 'sent') {
        // data is a request object: { id, toName, toEmail }
        const info = document.createElement('div');
        info.className = 'friend-item-info';

        const itemName = document.createElement('div');
        itemName.className = 'friend-item-name';
        itemName.textContent = data.toName;

        const itemEmail = document.createElement('div');
        itemEmail.className = 'friend-item-email';
        itemEmail.textContent = data.toEmail || '이메일 정보 없음';

        info.appendChild(itemName);
        info.appendChild(itemEmail);
        item.appendChild(info);

        const cancelButton = document.createElement('button');
        cancelButton.className = 'cancel-btn';
        cancelButton.textContent = '취소';
        cancelButton.onclick = () => cancelFriendRequest(data.id);
        actions.appendChild(cancelButton);
    } else if (type === 'received') { // New block for received requests
        // data is a request object: { id, toName, toEmail }
        const info = document.createElement('div');
        info.className = 'friend-item-info';

        const itemName = document.createElement('div');
        itemName.className = 'friend-item-name';
        itemName.textContent = data.toName;

        const itemEmail = document.createElement('div');
        itemEmail.className = 'friend-item-email';
        itemEmail.textContent = data.toEmail || '이메일 정보 없음';

        info.appendChild(itemName);
        info.appendChild(itemEmail);
        item.appendChild(info);

        const acceptButton = document.createElement('button');
        acceptButton.className = 'accept-btn';
        acceptButton.textContent = '수락';
        acceptButton.onclick = () => acceptFriendRequest(data.id);
        
        const declineButton = document.createElement('button');
        declineButton.className = 'decline-btn';
        declineButton.textContent = '거절';
        declineButton.onclick = () => declineFriendRequest(data.id);

        actions.appendChild(acceptButton);
        actions.appendChild(declineButton);
    }

    item.appendChild(actions);
    return item;
}
// --- 친구 관련 액션 핸들러 ---

async function updateFriendRequestStatus(id, status) {
    try {
        const response = await fetchWithAuth(`/friend/status/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            },
            body: JSON.stringify({ status: status })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '요청 처리에 실패했습니다.');
        }

        alert(`요청을 ${status === 'ACCEPT' ? '수락' : '거절'}했습니다.`);
        await fetchFriendData(); // Refresh the friend list

    } catch (error) {
        console.error('친구 요청 처리 중 오류:', error);
        alert(`친구 요청 처리 중 오류가 발생했습니다: ${error.message}`);
    }
}


function acceptFriendRequest(id) {
    updateFriendRequestStatus(id, 'ACCEPT');
}

function declineFriendRequest(id) {
    updateFriendRequestStatus(id, 'REJECT');
}

function cancelFriendRequest(id) {
    console.log(`Cancelling friend request with id: ${id}`);
    // 여기에 취소 API 호출 로직 추가
}

function removeFriend(name) {
    console.log(`Removing friend: ${name}`);
    // 여기에 친구 삭제 API 호출 로직 추가
}

function blockFriend(name) {
    console.log(`Blocking friend: ${name}`);
    alert(`${name}님을 차단했습니다. (API 연동 예정)`);
    // 여기에 친구 차단 API 호출 로직 추가
}

async function addFriend(friendEmail) {
    const accessToken = sessionStorage.getItem('accessToken');
    const grantType = sessionStorage.getItem('grantType') || 'Bearer';

    if (!accessToken) {
        alert('로그인이 필요합니다.');
        return;
    }

    let response = await fetch('/add/friend', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `${grantType} ${accessToken}`
        },
        body: JSON.stringify({ friendEmail: friendEmail })
    });

    if (response.ok) {
        alert('친구 추가 요청을 보냈습니다.');
    } else {
        const errorData = await response.json();
        alert(`친구 추가에 실패했습니다: ${errorData.message}`);
    }
}
