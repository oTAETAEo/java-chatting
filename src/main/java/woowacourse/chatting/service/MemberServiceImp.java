package woowacourse.chatting.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.member.FriendRelation;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.repository.member.MemberRepository;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImp implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));
    }

    @Override
    public Member findByEmailMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));
    }

    @Override
    public FriendRelation addFriend(Member my, String friendEmail) {
        return FriendRelation.builder()
                .from(my)
                .to(findByEmailMember(friendEmail))
                .build();
    }

    public Long save(AddMemberRequest dto) {
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
