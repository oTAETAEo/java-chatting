package woowacourse.chatting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFriendDto {

    @NotBlank
    private String friendEmail;
}
