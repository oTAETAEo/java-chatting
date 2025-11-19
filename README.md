## 📖 Web-Socket(STOMP), Spring Security 학습 Chatting 프로젝트

---

### 4주 진행 목표

|      **목표**      | **세부 실행 내용 (구현할 기능)**                                                                               |     **기대 결과물**     |
|:----------------:|-----------------------------------------------------------------------------------------------------|:------------------:|
|   **보안 기반 확립**   | 회원가입 및 로그인 REST API 완성. Spring Security 기본 설정 및 **JWT 발급/검증 필터** 구현. Postman 등으로 REST API 인증 성공 확인. |  JWT 기반 인증 시스템 동작  |
| **WebSocket 연결** | Spring WebSocket 설정 및 STOMP 브로커 활성화. 간단한 HTML/JS 페이지를 만들어 서버와 **단일 채널로 메시지를 주고받는** 기능 검증.           |  클라이언트-서버 실시간 통신   |
| **채팅방 관리 CRUD**  | 채팅방(Room) 엔티티 설계 및 DB 저장. 방 생성, 목록 조회, 삭제 기능을 REST API로 구현. **(실시간 통신은 제외)**                        | 채팅방 목록 조회 REST API |

|         **목표**         | **세부 실행 내용 (구현할 기능)**                                                                                                                           |      **기대 결과물**       |
|:----------------------:|-------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------:|
| **JWT & WebSocket 통합** | WebSocket Handshake 인터셉터(`ChannelInterceptor`또는`HandshakeInterceptor`) 구현. 연결 요청 시 Header에서**JWT를 추출**하고 사용자 인증 정보(Principal)를 설정하여**보안 연동**완성. | 인증된 사용자만 WebSocket 연결 |
|      **방 기반 메시징**      | 메시지 전송 시`@MessageMapping`을 통해 메시지를 받아,`SimpMessagingTemplate`로**구독 중인 방 토픽**(`/topic/room/{roomId}`)으로 메시지를 라우팅. 채팅 메시지 DB 저장 로직 구현.            |    실시간 방 기반 채팅 동작     |
|    **최종 테스트 & 정리**     | 다중 사용자(다중 탭) 환경에서 실시간 채팅 및 인증/인가 테스트. 미션 중 발생한 문제 해결 과정, 최종 아키텍처 다이어그램 등을 포함한**기술 보고서**작성.                                                      | 최종 동작하는 애플리케이션 및 보고서  |


---

### 문서

| 정리 문서 |
|:--:|
| <a href="https://www.notion.so/Spring-Security-2a2026cb1f90808aa352db768c6ebc7e?source=copy_link/" target="_blank">Spring-Security 인증 구조 정리 문서</a>        |
| <a href="https://www.notion.so/Spring-Websocket-STOMP-2a5026cb1f9080b09718d7599b31f41e?source=copy_link" target="_blank">Spring-Websocket (STOMP)정리 문서</a> |
| <a href="https://www.notion.so/CSRF-2a2026cb1f90809aa001d85b9e699c59?source=copy_link" target="_blank">CSRF 정리 문서</a>                                                                                                |


<br>

---

### 기술 스택
    - 빌드 도구: Gradle
    - 언어: Java 21
    - 프레임워크: Spring Boot (버전 3.5.7)
    - 보안: Spring Security, JWT (JSON Web Token)
    - 웹: Spring Boot Starter Web, Spring Boot Starter WebSocket
    - 데이터베이스: Spring Boot Starter Data JPA, H2 Database
    - 유효성 검사: Spring Boot Starter Validation
    - 개발 보조: Project Lombok

<br>

---

### 🚀 실행 방법

    사전 요구사항
    - Java 21
    - Git

    1. 저장소 클론

    2. application-jwt.yml 파일 생성 후 JWT 시크릿 키 추가.
    (명령어)
    - [mac] openssl rand -hex 64
    - 윈도우 사용자는 git bash 에 위 명령어 입력

    jwt: 
        secret: <생성된 키>

    3. 애플리케이션 실행
        localhosh:8080 접속
        2개의 브라우저로 접속 해 로그인 하는걸 추천

    4. 테스트 계정 로그인
        (1번 계정)
        아이디  : test@test.com
        비밀번호 : 1111
        
        (2번 계정)
        아이디  : 1@1
        비밀번호 : 1111


<img width="1078" height="601" alt="Image" src="https://github.com/user-attachments/assets/b310b54b-ac9b-42af-abce-f7a0f0d402ca" />

<img width="1077" height="601" alt="Image" src="https://github.com/user-attachments/assets/1c3c3ff5-e5cd-4962-a843-60b5b633fdcd" />





