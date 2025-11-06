// DOM 요소 가져오기
const messageBox = document.getElementById('chat-messages');
const messageInput = document.getElementById('message-input');
const usernameInput = document.getElementById('username-input');
const sendButton = document.getElementById('send-button');
const statusMessage = document.getElementById('status-message');
const disconnectButton = document.getElementById('disconnect-button');

// WebSocket 연결 정보
// WSS(Secure WebSocket)를 권장하지만, 로컬 테스트를 위해 WS를 사용합니다.
// Spring Boot에서 STOMP나 기본 WebSocket 핸들러를 설정해야 합니다.
const WS_URL = 'ws://localhost:8080/chat';

let websocket;
let currentUserName = "Guest"; // 챗 메시지 전송에 사용될 사용자 이름

// --- JWT 토큰의 Payload를 디코딩하는 함수 (추가) ---
function decodeJwt(token) {
    try {
        // JWT의 Payload 부분은 두 번째 요소입니다.
        const base64Url = token.split('.')[1];

        // Base64URL을 Base64로 변환하고, 패딩을 추가
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        // Base64 디코딩 및 URI 디코딩을 통해 JSON 문자열 획득
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("JWT 디코딩 실패:", e);
        return null;
    }
}


// --- 메시지를 화면에 표시하는 함수 ---
function displayMessage(user, message) {
    const messageElement = document.createElement('p');
    messageElement.classList.add('message');

    // XSS 방지를 위한 텍스트 안전 처리 (innerText 사용)
    const safeMessage = document.createElement('span');
    safeMessage.innerText = message;

    const userSpan = document.createElement('strong');
    userSpan.innerText = user + ': ';

    messageElement.appendChild(userSpan);
    messageElement.appendChild(safeMessage);

    messageBox.appendChild(messageElement);
    // 스크롤 최하단으로 자동 이동
    messageBox.scrollTop = messageBox.scrollHeight;
}

// --- WebSocket 연결 함수 (수정: 사용자 이름 추출) ---
function connectWebSocket() {
    // 저장된 토큰을 가져옵니다.
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        statusMessage.textContent = '❌ 오류: 액세스 토큰이 없습니다. 로그인이 필요합니다.';
        return;
    }

    // 🔥 토큰에서 사용자 이름 추출 및 설정
    const decodedToken = decodeJwt(accessToken);
    if (decodedToken && decodedToken.name) { // 서버 JWT Payload의 'name' 키를 사용한다고 가정
        currentUserName = decodedToken.name;
        usernameInput.value = currentUserName;
        usernameInput.disabled = true;       // 추출 후에는 수정 못하게 비활성화
    } else {
        statusMessage.textContent = '⚠️ 오류: 토큰에서 사용자 이름을 추출할 수 없습니다. (디버깅 필요)';
        // 추출 실패 시 채팅 기능을 비활성화할 수도 있습니다.
        // return;
    }

    // 서버에 토큰을 쿼리 파라미터로 전달하여 연결 시 인증을 시도합니다.
    const urlWithToken = `${WS_URL}?token=${accessToken}`;

    websocket = new WebSocket(urlWithToken);

    // 1. 연결 성공 시
    websocket.onopen = () => {
        statusMessage.textContent = '🟢 연결되었습니다. 사용자: ' + currentUserName;
        sendButton.disabled = false;
        disconnectButton.disabled = false;
        console.log('WebSocket 연결 성공.');
    };

    // 2. 메시지 수신 시
    websocket.onmessage = (event) => {
        try {
            // 메시지를 JSON 형태로 파싱 (서버는 JSON을 보낸다고 가정)
            const messageData = JSON.parse(event.data);
            displayMessage(messageData.sender || '시스템', messageData.content);
        } catch (e) {
            // 단순 텍스트 메시지일 경우
            displayMessage('시스템', event.data);
        }
    };

    // 3. 연결 에러 발생 시
    websocket.onerror = (error) => {
        statusMessage.textContent = '🔴 연결 오류 발생.';
        console.error('WebSocket 오류:', error);
    };

    // 4. 연결 종료 시
    websocket.onclose = (event) => {
        statusMessage.textContent = event.wasClean ? '🟡 연결이 정상적으로 종료되었습니다.' : '🔴 연결이 비정상적으로 종료되었습니다.';
        sendButton.disabled = true;
        disconnectButton.disabled = true;
        console.log('WebSocket 연결 종료:', event);
    };
}

// --- 메시지 전송 함수 (수정: currentUserName 사용) ---
function sendMessage() {
    const message = messageInput.value.trim();
    const user = currentUserName; // 🔥 토큰에서 추출된 이름 사용

    if (!message || !user || !websocket || websocket.readyState !== WebSocket.OPEN) {
        alert("메시지를 입력하고 연결 상태를 확인해주세요.");
        return;
    }

    // 서버로 전송할 JSON 메시지 구조
    const chatMessage = {
        sender: user,
        content: message
    };

    // JSON 문자열로 변환하여 서버에 전송
    websocket.send(JSON.stringify(chatMessage));

    messageInput.value = ''; // 입력창 비우기
}

// --- 이벤트 리스너 ---
sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
disconnectButton.addEventListener('click', () => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.close();
    }
    // 연결 종료 후 로그인 페이지로 리다이렉션
    window.location.href = 'index.html';
    // 로컬 스토리지에 저장된 토큰을 지우는 것도 고려할 수 있습니다.
    // localStorage.removeItem('accessToken');
});

// 페이지 로드 시 WebSocket 연결 시작
window.onload = connectWebSocket;