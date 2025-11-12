package woowacourse.chatting.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseDto {

    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;

    public ResponseDto(String message) {
        this.message = message;
    }
}
