// WebSocket 연결 정보
// WSS(Secure WebSocket)를 권장하지만, 로컬 테스트를 위해 WS를 사용합니다.
// Spring WebSocketConfig에서 설정한 엔드포인트 경로를 사용합니다.
const WS_ENDPOINT = 'http://localhost:8080/ws/chat'; // SockJS는 HTTP 엔드포인트를 사용합니다.

let stompClient = null; // Stomp Client 객체
let currentUserName = "Guest"; // 챗 메시지 전송에 사용될 사용자 이름

// --- JWT 토큰의 Payload를 디코딩하는 함수 (변경 없음) ---
function decodeJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("JWT 디코딩 실패:", e);
        return null;
    }
}

// --- 메시지를 화면에 표시하는 함수 (변경 없음) ---
function displayMessage(user, message) {
    const messageBox = document.getElementById('chat-messages');
    if (!messageBox) return;

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

// --- API 요청 및 자동 토큰 재발급을 위한 fetch 래퍼 함수 (변경 없음) ---
// (JWT 토큰 재발급 로직은 WebSocket/STOMP 연결 로직과 분리하여 그대로 유지합니다.)
async function fetchWithAuth(url, options) {
    // ... 기존 코드와 동일하게 유지 ...
    let response = await fetch(url, options);

    if (response.status === 401) {
        console.log("[Auth] 401 Unauthorized. 토큰 재발급을 시도합니다.");

        try {
            const refreshResponse = await fetch('/jwt/refresh', { method: 'GET' });
            if (!refreshResponse.ok) { throw new Error('토큰 재발급에 실패했습니다.'); }

            const tokenData = await refreshResponse.json();
            const newAccessToken = tokenData.accessToken;
            const newGrantType = tokenData.grantType;

            localStorage.setItem('accessToken', newAccessToken);
            localStorage.setItem('grantType', newGrantType);
            console.log("[Auth] 새로운 액세스 토큰을 발급받아 저장했습니다.");

            const newOptions = {...options};
            if (!newOptions.headers) newOptions.headers = {}; // headers 객체 없으면 생성
            newOptions.headers['Authorization'] = `${newGrantType} ${newAccessToken}`;

            console.log("[Auth] 새로운 토큰으로 원래 요청을 재시도합니다.");
            response = await fetch(url, newOptions);

        } catch (error) {
            console.error("[Auth] 토큰 재발급 과정에서 오류가 발생했습니다:", error);
            window.location.href = 'index.html';
            return response;
        }
    }
    return response;
}


// --- WebSocket (STOMP) 연결 및 초기화 함수 ---
function connectWebSocket() {
    console.log('[Debug] connectWebSocket() 함수 실행 시작 (STOMP 모드).');

    // 1. 모든 DOM 요소를 이 함수 내에서 가져옵니다.
    const messageInput = document.getElementById('message-input');
    const usernameInput = document.getElementById('username-input');
    const sendButton = document.getElementById('send-button');
    const statusMessage = document.getElementById('status-message');
    const disconnectButton = document.getElementById('disconnect-button');
    const testApiButton = document.getElementById('test-api-button');
    const logoutButton = document.getElementById('logout-button');

    if (!statusMessage || !messageInput || !usernameInput || !sendButton) {
        console.error("[Debug] 필수 DOM 요소를 찾지 못했습니다. 함수를 중단합니다.");
        return;
    }

    const accessToken = localStorage.getItem('accessToken');
    const grantType = localStorage.getItem('grantType') || 'Bearer';

    if (!accessToken) {
        statusMessage.textContent = '❌ 오류: 액세스 토큰이 없습니다. 로그인이 필요합니다.';
        console.error('[Debug] accessToken이 없으므로 connectWebSocket() 함수를 여기서 중단합니다.');
        return;
    }

    // 토큰에서 사용자 이름 추출 및 설정
    const decodedToken = decodeJwt(accessToken);
    if (decodedToken && decodedToken.name) {
        currentUserName = decodedToken.name;
        usernameInput.value = currentUserName;
        usernameInput.disabled = true;
    } else {
        statusMessage.textContent = '⚠️ 오류: 토큰에서 사용자 이름을 추출할 수 없습니다.';
    }

    // 2. SockJS를 사용하여 WebSocket 연결을 시도합니다.
    console.log('[Debug] SockJS 연결을 시도합니다. Endpoint:', WS_ENDPOINT);
    const socket = new SockJS(WS_ENDPOINT);
    stompClient = Stomp.over(socket);

    // 3. STOMP 연결을 시도하고, CONNECT 헤더에 토큰을 포함합니다.
    const headers = {
        'Authorization': `${grantType} ${accessToken}`
        // Spring Security ChannelInterceptor에서 이 헤더를 읽어 인증합니다.
    };

    stompClient.connect(headers, (frame) => {
        // 🟢 연결 성공 시
        statusMessage.textContent = '🟢 STOMP 연결되었습니다. 사용자: ' + currentUserName;
        sendButton.disabled = false;
        disconnectButton.disabled = false;
        console.log('[Debug] STOMP 연결 성공. Connected: ' + frame);

        // 4. 공용 채팅 채널 구독
        // Spring WebSocketConfig에서 설정한 /topic/chat 경로를 구독합니다.
        stompClient.subscribe('/topic/chat', (message) => {
            const messageData = JSON.parse(message.body);
            displayMessage(messageData.sender || '시스템', messageData.content);
        });

        // 5. 개인 메시지 채널 구독 (선택 사항: 1:1 채팅 구현 시)
        // Spring WebSocketConfig에서 설정한 /user 경로를 활용
        // stompClient.subscribe('/user/queue/messages', (message) => {
        //     const messageData = JSON.parse(message.body);
        //     displayMessage('개인 메시지 (FROM: ' + messageData.sender + ')', messageData.content);
        // });


    }, (error) => {
        // 🔴 연결 실패 시
        statusMessage.textContent = '🔴 STOMP 연결 오류 발생: ' + error;
        sendButton.disabled = true;
        disconnectButton.disabled = true;
        console.error('[Debug] STOMP 연결 오류:', error);
    });

    // --- 메시지 전송 함수 (STOMP SEND 사용) ---
    function sendMessage() {
        const message = messageInput.value.trim();
        const user = currentUserName;

        if (!message || !user || !stompClient) {
            console.warn("메시지 입력 또는 STOMP 클라이언트 확인 필요.");
            return;
        }

        const chatMessage = {
            sender: user,
            content: message
        };

        // Spring WebSocketConfig에서 설정한 /app/chat 경로로 메시지를 보냅니다.
        // 이 메시지는 서버의 @MessageMapping("/chat") 컨트롤러로 라우팅됩니다.
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));

        messageInput.value = '';
    }

    // --- /test GET 요청 함수 (변경 없음) ---
    async function sendTestApiRequest() {
        console.log('[Debug] "test-api-button" clicked. Starting API request.');

        const API_URL = 'http://localhost:8080/test';
        const accessToken = localStorage.getItem('accessToken');
        const grantType = localStorage.getItem('grantType') || 'Bearer';

        const apiResultMessage = document.getElementById('api-result-message');

        if (!accessToken) {
            console.error('[Debug] Access token is missing. Aborting request.');
            if (apiResultMessage) {
                apiResultMessage.textContent = '❌ 토큰 없음. 로그인 해주세요.';
                apiResultMessage.style.color = 'red';
            }
            return;
        }

        if (apiResultMessage) {
            apiResultMessage.textContent = 'API 요청 중...';
            apiResultMessage.style.color = '#007bff';
        }

        try {
            const response = await fetchWithAuth(API_URL, {
                method: 'GET',
                headers: {
                    'Authorization': `${grantType} ${accessToken}`
                }
            });

            const responseBody = await response.text();

            if (response.ok) {
                if (apiResultMessage) {
                    apiResultMessage.textContent = `✅ API 성공: ${responseBody}`;
                    apiResultMessage.style.color = 'green';
                }
            } else {
                const displayError = responseBody.length > 100 ? responseBody.substring(0, 100) + '...' : responseBody;
                if (apiResultMessage) {
                    apiResultMessage.textContent = `❌ API 실패 (${response.status}): ${displayError}`;
                    apiResultMessage.style.color = 'red';
                }
            }
        } catch (error) {
            if (apiResultMessage) {
                apiResultMessage.textContent = `❌ 네트워크 오류: ${error.message}`;
                apiResultMessage.style.color = 'red';
            }
        }
    }


    // --- 로그아웃 함수 (변경 없음) ---
    async function logout() {
        const accessToken = localStorage.getItem('accessToken');
        const grantType = localStorage.getItem('grantType') || 'Bearer';

        try {
            const response = await fetchWithAuth('/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `${grantType} ${accessToken}`
                }
            });

            // 로그아웃 API 성공 여부와 상관없이 로컬 스토리지 비우기
            localStorage.removeItem('accessToken');
            localStorage.removeItem('grantType');
            window.location.href = 'index.html';

        } catch (error) {
            console.error('Error during logout, but proceeding with redirection:', error);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('grantType');
            window.location.href = 'index.html';
        }
    }


    // --- 이벤트 리스너 연결 ---
    console.log('[Debug] 이벤트 리스너를 연결합니다...');
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    disconnectButton.addEventListener('click', () => {
        if (stompClient) {
            // STOMP 연결 해제 (웹소켓도 함께 종료됨)
            stompClient.disconnect(() => {
                console.log("STOMP 연결이 정상적으로 해제되었습니다.");
                statusMessage.textContent = '🟡 STOMP 연결이 해제되었습니다.';
            });
        }
        // 연결 종료 후 로그인 페이지로 리다이렉션 (필요하다면)
        // window.location.href = 'index.html';
    });


    if (testApiButton) {
        testApiButton.addEventListener('click', sendTestApiRequest);
    }
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
}

// 페이지 로드 시 WebSocket 연결 시작
window.onload = connectWebSocket;