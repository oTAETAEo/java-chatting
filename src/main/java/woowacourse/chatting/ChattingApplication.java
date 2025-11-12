package woowacourse.chatting;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.service.MemberService;

@SpringBootApplication
public class ChattingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChattingApplication.class, args);
    }

    @Autowired
    MemberService memberService;

    @PostConstruct
    public void init() {
        memberService.save(new AddMemberRequest(
                "test@test.com"
                , "홍길동"
                , "1111")
        );
        memberService.save(new AddMemberRequest(
                "1@1"
                , "강감찬"
                , "1111")
        );
    }
}
