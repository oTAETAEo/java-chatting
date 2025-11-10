package woowacourse.chatting;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import woowacourse.chatting.domain.message.ChatRoom;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.repository.message.ChatRoomRepository;
import woowacourse.chatting.service.MemberService;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class ChattingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChattingApplication.class, args);
    }

    @Autowired
    MemberService memberService;

    @PostConstruct
    public void init(){
        memberService.save(new AddMemberRequest(
                "test@test.com"
                ,"홍길동"
                ,"1111")
        );
        memberService.save(new AddMemberRequest(
                "1@1"
                ,"강감찬"
                ,"1111")
        );
    }
}
