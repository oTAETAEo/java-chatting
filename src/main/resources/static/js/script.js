// 폼 요소와 메시지 영역 가져오기
const signupForm = document.getElementById('signup-form');
const loginForm = document.getElementById('login-form');
const signupMessage = document.getElementById('signup-message');
const loginMessage = document.getElementById('login-message');
const showSignupBtn = document.getElementById('show-signup');
const showLoginBtn = document.getElementById('show-login');

// --- 폼 전환 기능 (유지) ---
function switchForm(formToShow, formToHide) {
    // CSS 클래스 전환 로직은 동일
    formToHide.classList.remove('active');
    formToHide.classList.add('hidden');

    setTimeout(() => {
        formToShow.classList.remove('hidden');
        formToShow.classList.add('active');
    }, 50);
}

showSignupBtn.addEventListener('click', () => {
    switchForm(signupForm, loginForm);
    loginMessage.textContent = '';
});

showLoginBtn.addEventListener('click', () => {
    switchForm(loginForm, signupForm);
    signupMessage.textContent = '';
});


// --- 회원가입 API 요청 처리 ---
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // 기본 폼 제출 동작 방지
    signupMessage.textContent = '회원가입 요청 중...';
    signupMessage.className = 'message';

    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;
    // 'member-name' ID는 HTML에 있어야 작동합니다.
    const name = document.getElementById('member-name').value;

    const signupData = {
        email: email,
        password: password,
        name: name
    };

    // **회원가입 API 엔드포인트**
    const API_URL = 'http://localhost:8080/sign-up';

    try {
        // 실제 fetch API 호출
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(signupData),
        });

        // 응답 상태 확인
        if (response.status === 200) {
            // 성공
            const result = await response.json();
            signupMessage.textContent = `회원가입 성공: ${result.message || '환영합니다!'}`;
            signupMessage.classList.add('success');
            signupForm.reset();
        } else {
            // 실패
            const errorData = await response.json();
            throw new Error(errorData.message || `이메일, 비밀번호, 이름을 입력하시오  (상태 코드: ${response.status})`);
        }

    } catch (error) {
        // 오류 처리
        console.error('회원가입 요청 실패:', error);
        signupMessage.textContent = `회원가입 오류: ${error.message}`;
        signupMessage.classList.add('error');
    }
});


// --- 🔥 로그인 API 요청 처리 (수정된 부분) ---
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // 기본 폼 제출 동작 방지
    loginMessage.textContent = '로그인 요청 중...';
    loginMessage.className = 'message';

    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    const loginData = {
        email: email,
        password: password
    };

    // **로그인 API 엔드포인트**
    const API_URL = 'http://localhost:8080/sign-in';

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData),
        });

        if (response.status === 200) {
            // 200 OK: 로그인 성공
            const result = await response.json();

            // 🔥🔥🔥 추가된 토큰 저장 로직 🔥🔥🔥
            if (result.token) {
                // 'accessToken'이라는 키로 액세스 토큰 값을 localStorage에 저장
                localStorage.setItem('accessToken', result.token);
                // grantType (Bearer)도 필요하다면 함께 저장하여 나중에 사용
                if (result.grantType) {
                    localStorage.setItem('grantType', result.grantType);
                }
                console.log("로그인 성공! 액세스 토큰이 localStorage에 저장되었습니다.");
            }

            loginMessage.textContent = `로그인 성공! ${result.message || ''}`;
            loginMessage.classList.add('success');

            window.location.href = 'chat.html';
        } else {
            // 로그인 실패
            const errorData = await response.json();
            throw new Error(errorData.message || `로그인 실패 (상태 코드: ${response.status})`);
        }

    } catch (error) {
        // 오류 처리
        console.error('로그인 요청 실패:', error);
        loginMessage.textContent = `로그인 오류: ${error.message}`;
        loginMessage.classList.add('error');
    }
});