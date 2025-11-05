// 폼 요소와 메시지 영역 가져오기
const signupForm = document.getElementById('signup-form');
const loginForm = document.getElementById('login-form');
const signupMessage = document.getElementById('signup-message');
const loginMessage = document.getElementById('login-message'); // 사용하지 않지만 DOM 요소는 남김
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


// --- 회원가입 API 요청 처리 (간소화) ---
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // 기본 폼 제출 동작 방지
    signupMessage.textContent = '회원가입 요청 중...';
    signupMessage.className = 'message';

    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;
    const name = document.getElementById('member-name').value;

    const signupData = {
        email: email,
        password: password,
        name: name
    };

    // **회원가입 API 엔드포인트**
    const API_URL = 'http://localhost:8080/signUp';

    try {
        // 실제 fetch API 호출
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 필요하다면 다른 헤더 (예: Authorization) 추가
            },
            body: JSON.stringify(signupData),
        });

        // 응답 상태 확인
        if (response.status === 200) {
            // 성공 (상태 코드 200-299)
            const result = await response.json(); //  서버에서 보낸 응답 JSON 파싱
            signupMessage.textContent = `회원가입 성공: ${result.message || '환영합니다!'}`;
            signupMessage.classList.add('success');
            signupForm.reset(); // 폼 초기화
        } else {
            // 실패 (상태 코드 400, 500 등)
            const errorData = await response.json();
            throw new Error(errorData.message || `이메일, 비밀번호, 이름을 입력하시오  (상태 코드: ${response.status})`);
        }

    } catch (error) {
        // 네트워크 오류 또는 throw된 오류 처리
        console.error('회원가입 요청 실패:', error);
        signupMessage.textContent = `회원가입 오류: ${error.message}`;
        signupMessage.classList.add('error');
    }
});
