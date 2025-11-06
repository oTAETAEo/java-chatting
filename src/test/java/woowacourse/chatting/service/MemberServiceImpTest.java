package woowacourse.chatting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import woowacourse.chatting.dto.AddMemberRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class MemberServiceImpTest {

    @Autowired
    private MemberServiceImp memberServiceImp;

    @DisplayName("새로운 사용자 정보를 받아 회원을 저장하고, 저장된 회원의 ID를 반환한다.")
    @Test
    void save_Success(){
        // given
        AddMemberRequest request = new AddMemberRequest("test@test.com", "test", "test1234");

        // when
        Long resultId = memberServiceImp.save(request);

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
        memberServiceImp.save(request);

        // then
        assertThatThrownBy(() -> memberServiceImp.save(duplicateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

}