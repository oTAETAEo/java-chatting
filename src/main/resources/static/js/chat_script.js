// WebSocket 연결 정보
// WSS(Secure WebSocket)를 권장하지만, 로컬 테스트를 위해 WS를 사용합니다.
const WS_URL = 'ws://localhost:8080/chat';

let websocket;
let currentUserName = "Guest"; // 챗 메시지 전송에 사용될 사용자 이름

// --- JWT 토큰의 Payload를 디코딩하는 함수 ---
function decodeJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
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

// --- WebSocket 연결 및 초기화 함수 (모든 DOM 접근은 여기서 이루어집니다.) ---
function connectWebSocket() {
    console.log('[Debug] connectWebSocket() 함수 실행 시작.');

    // 1. 모든 DOM 요소를 이 함수 내에서 가져옵니다. (DOM 로드 보장)
    const messageInput = document.getElementById('message-input');
    const usernameInput = document.getElementById('username-input');
    const sendButton = document.getElementById('send-button');
    const statusMessage = document.getElementById('status-message');
    const disconnectButton = document.getElementById('disconnect-button');
    const testApiButton = document.getElementById('test-api-button');
    const apiResultMessage = document.getElementById('api-result-message');

    // 필수 요소 검증
    if (!statusMessage || !messageInput || !usernameInput || !sendButton) {
        console.error("[Debug] 필수 DOM 요소를 찾지 못했습니다. 함수를 중단합니다.");
        return;
    }
    console.log('[Debug] 모든 필수 DOM 요소를 찾았습니다.');

    const accessToken = localStorage.getItem('accessToken');
    console.log('[Debug] localStorage에서 "accessToken"을 조회한 결과:', accessToken);

    if (!accessToken) {
        statusMessage.textContent = '❌ 오류: 액세스 토큰이 없습니다. 로그인이 필요합니다.';
        console.error('[Debug] accessToken이 없으므로 connectWebSocket() 함수를 여기서 중단합니다.');
        return;
    }

    console.log('[Debug] accessToken을 찾았으므로 계속 진행합니다.');

    // 🔥 토큰에서 사용자 이름 추출 및 설정
    const decodedToken = decodeJwt(accessToken);
    if (decodedToken && decodedToken.name) {
        currentUserName = decodedToken.name;
        usernameInput.value = currentUserName;
        usernameInput.disabled = true;
    } else {
        statusMessage.textContent = '⚠️ 오류: 토큰에서 사용자 이름을 추출할 수 없습니다. (디버깅 필요)';
    }

    // 서버에 토큰을 쿼리 파라미터로 전달하여 연결 시 인증을 시도합니다.
    const urlWithToken = `${WS_URL}?token=${accessToken}`;
    console.log('[Debug] WebSocket 연결을 시도합니다. URL:', urlWithToken);

    websocket = new WebSocket(urlWithToken);

    // 1. 연결 성공 시
    websocket.onopen = () => {
        statusMessage.textContent = '🟢 연결되었습니다. 사용자: ' + currentUserName;
        sendButton.disabled = false;
        disconnectButton.disabled = false;
        console.log('[Debug] WebSocket 연결 성공.');
    };

    websocket.onmessage = (event) => {
        try {
            const messageData = JSON.parse(event.data);
            displayMessage(messageData.sender || '시스템', messageData.content);
        } catch (e) {
            displayMessage('시스템', event.data);
        }
    };
    websocket.onerror = (error) => {
        statusMessage.textContent = '🔴 연결 오류 발생.';
        console.error('[Debug] WebSocket 오류:', error);
    };
    websocket.onclose = (event) => {
        statusMessage.textContent = event.wasClean ? '🟡 연결이 정상적으로 종료되었습니다.' : '🔴 연결이 비정상적으로 종료되었습니다.';
        sendButton.disabled = true;
        disconnectButton.disabled = true;
        console.log('[Debug] WebSocket 연결 종료:', event);
    };


    // --- 메시지 전송 함수 (내부 정의) ---
    function sendMessage() {
        const message = messageInput.value.trim();
        const user = currentUserName;

        if (!message || !user || !websocket || websocket.readyState !== WebSocket.OPEN) {
            console.warn("메시지 입력 또는 연결 상태 확인 필요.");
            return;
        }

        const chatMessage = {
            sender: user,
            content: message
        };

        websocket.send(JSON.stringify(chatMessage));

        messageInput.value = '';
    }

    // --- /test GET 요청 함수 (내부 정의) ---
    async function sendTestApiRequest() {
        console.log('[Debug] "test-api-button" clicked. Starting API request.');

        const API_URL = 'http://localhost:8080/test';
        const accessToken = localStorage.getItem('accessToken');
        const grantType = localStorage.getItem('grantType') || 'Bearer';

        console.log('[Debug] Retrieved accessToken from localStorage:', accessToken);
        console.log('[Debug] Retrieved grantType:', grantType);

        const apiResultMessage = document.getElementById('api-result-message');

        if (!accessToken) {
            console.error('[Debug] Access token is missing. Aborting request.');
            if (apiResultMessage) {
                apiResultMessage.textContent = '❌ 토큰 없음. 로그인 해주세요.';
                apiResultMessage.style.color = 'red';
            }
            return;
        }

        console.log('[Debug] Preparing to send fetch request to:', API_URL);
        if (apiResultMessage) {
            apiResultMessage.textContent = 'API 요청 중...';
            apiResultMessage.style.color = '#007bff';
        }

        try {
            const response = await fetch(API_URL, {
                method: 'GET',
                headers: {
                    'Authorization': `${grantType} ${accessToken}`
                }
            });

            console.log('[Debug] Received response from server. Status:', response.status);
            const responseBody = await response.text();
            console.log('[Debug] Response body:', responseBody);

            if (response.ok) {
                if (apiResultMessage) {
                    apiResultMessage.textContent = `✅ API 성공: ${responseBody}`;
                    apiResultMessage.style.color = 'green';
                }
            } else {
                const displayError = responseBody.length > 100 ? responseBody.substring(0, 100) + '...' : responseBody;
                console.error(`[Debug] API request failed with status ${response.status}.`);
                if (apiResultMessage) {
                    apiResultMessage.textContent = `❌ API 실패 (${response.status}): ${displayError}`;
                    apiResultMessage.style.color = 'red';
                }

                if (response.status === 401) {
                    console.error("[Debug] Authentication failed (401). Redirecting to index.html.");
                    window.location.href = 'index.html';
                }
            }
        } catch (error) {
            console.error("[Debug] A network error occurred during the fetch request:", error);
            if (apiResultMessage) {
                apiResultMessage.textContent = `❌ 네트워크 오류: ${error.message}`;
                apiResultMessage.style.color = 'red';
            }
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
        if (websocket && websocket.readyState === WebSocket.OPEN) {
            websocket.close();
        }
        // 연결 종료 후 로그인 페이지로 리다이렉션
        window.location.href = 'index.html';
    });

    // 🔥 API 버튼 이벤트 리스너 연결
    if (testApiButton) {
        testApiButton.addEventListener('click', sendTestApiRequest);
        console.log('[Debug] "test-api-button"에 이벤트 리스너를 성공적으로 연결했습니다.');
    } else {
        console.error('[Debug] "test-api-button" 요소를 찾지 못해 이벤트 리스너를 연결할 수 없습니다.');
    }
}

// 페이지 로드 시 WebSocket 연결 시작
window.onload = connectWebSocket;