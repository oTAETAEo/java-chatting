package woowacourse.chatting.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.repository.member.MemberRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("이메일로 회원을 조회한다.")
    @Test
    void findByEmail() {

        // given
        String testEmail = "test@woowacourse.com";
        Member member = new Member(testEmail, "tae", "qwertyuiop");
        memberRepository.save(member); // DB에 저장

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(testEmail);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo(testEmail);
    }

    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다.")
    @Test
    void findByEmail_notExist() {
        // given
        String nonExistEmail = "nonexist@test.com";

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(nonExistEmail);

        // then
        assertThat(foundMember).isEmpty();
    }
}