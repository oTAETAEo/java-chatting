package woowacourse.chatting.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import woowacourse.chatting.repository.MemberRepository;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.dto.AddMemberRequest;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;

    public Long save(AddMemberRequest dto){
        duplicateMember(dto);

        Member member = Member.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(encoder.encode(dto.getPassword()))
                .build();

        memberRepository.save(member);
        return member.getId();
    }

    private void duplicateMember(AddMemberRequest dto) {
        memberRepository.findByEmail(dto.getEmail())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                });
    }
}
