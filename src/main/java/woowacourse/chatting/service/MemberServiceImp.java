package woowacourse.chatting.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.Member;
import woowacourse.chatting.dto.AddMemberRequest;
import woowacourse.chatting.jwt.JwtToken;
import woowacourse.chatting.jwt.JwtTokenProvider;
import woowacourse.chatting.repository.MemberRepository;

@Service
@AllArgsConstructor
@Slf4j
public class MemberServiceImp implements MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder encoder;

    @Transactional
    @Override
    public JwtToken singIn(String email, String password) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authenticate);
    }

    @Transactional
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
