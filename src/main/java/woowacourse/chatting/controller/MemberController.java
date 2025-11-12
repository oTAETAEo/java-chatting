package woowacourse.chatting.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.dto.ResponseDto;
import woowacourse.chatting.service.MemberService;

@RestController
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signUp")
    public ResponseEntity<ResponseDto> memberSignUp(@Validated @RequestBody AddMemberRequest dto){
        memberService.save(dto);
        return ResponseEntity.ok(new ResponseDto("회원가입이 완료되었습니다"));
    }
}
