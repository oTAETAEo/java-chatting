package woowacourse.chatting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.dto.auth.SignInDto;
import woowacourse.chatting.jwt.JwtToken;
import woowacourse.chatting.jwt.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtToken singIn(SignInDto signInDto) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(signInDto.getEmail(), signInDto.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authenticate);
    }
}
