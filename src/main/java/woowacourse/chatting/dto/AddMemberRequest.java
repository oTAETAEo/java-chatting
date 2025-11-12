package woowacourse.chatting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AddMemberRequest {

    @NotBlank(message = "이메일은 비어있을수 없습니다.")
    private String email;

    @NotBlank(message = "이름은 비어있을수 없습니다.")
    @Length(min = 3, max = 10, message = "이름의 길이는 3 ~ 10자 입니다.")
    private String name;

    @NotBlank(message = "비밀번호는 비어있을수 없습니다.")
    private String password;

    public AddMemberRequest(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }
}
