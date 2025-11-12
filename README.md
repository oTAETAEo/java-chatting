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



