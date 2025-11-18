package woowacourse.chatting.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;
import woowacourse.chatting.domain.auth.RefreshToken;
import woowacourse.chatting.domain.member.Member;
import woowacourse.chatting.dto.ResponseDto;
import woowacourse.chatting.jwt.JwtTokenProvider;
import woowacourse.chatting.repository.RefreshTokeRepository;
import woowacourse.chatting.service.MemberService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {

    private final RefreshTokeRepository refreshTokeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        if (!request.getMethod().equals("POST")) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        UUID subId = (UUID) authentication.getPrincipal();
        Member member = memberService.findBySubId(subId);

        Optional<RefreshToken> findRefreshToken = refreshTokeRepository.findByMemberId(member.getId());

        if (findRefreshToken.isEmpty()) {
            log.warn("해당 사용자의 RefreshToken이 존재하지 않음");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshTokeRepository.deleteByMemberId(member.getId());
        log.info("로그아웃 완료 - userId={}", member.getId());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ResponseDto("로그아웃이 정상적으로 처리되었습니다."));
    }
}
