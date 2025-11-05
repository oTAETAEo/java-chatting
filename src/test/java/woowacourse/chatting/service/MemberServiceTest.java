package woowacourse.chatting.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import woowacourse.chatting.Repositorty.MemberRepository;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.dto.AddMemberRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @DisplayName("새로운 사용자 정보를 받아 회원을 저장하고, 저장된 회원의 ID를 반환한다.")
    @Test
    void save_Success(){
        // given
        AddMemberRequest request = new AddMemberRequest("test@test.com", "test", "test1234");

        // when
        Long resultId = memberService.save(request);

        // then
        assertThat(resultId).isEqualTo(1L);
    }

    @DisplayName("중복되는 이메일이 있는경우 예외가 발생한다.")
    @Test
    void save_Ex(){
        // given
        AddMemberRequest request = new AddMemberRequest("test@test.com", "test", "test1234");
        AddMemberRequest duplicateRequest = new AddMemberRequest("test@test.com", "test", "test1234");

        // when
        memberService.save(request);

        // then
        assertThatThrownBy(() -> memberService.save(duplicateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

}