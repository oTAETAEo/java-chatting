package woowacourse.chatting.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ErrorResponse {

    private LocalDateTime timestamp = LocalDateTime.now(); // 오류 발생 시점
    private Map<String, String> error; // 오류 메시지 (예: "이미 존재하는 이메일입니다.")
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
        this.error = Map.of();
    }

    public ErrorResponse(Map<String, String> errors) {
        this.error = errors;
        this.message = "";
    }
}