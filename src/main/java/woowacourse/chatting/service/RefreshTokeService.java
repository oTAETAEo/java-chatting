package woowacourse.chatting.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.auth.RefreshToken;
import woowacourse.chatting.repository.RefreshTokeRepository;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokeService {

    private final RefreshTokeRepository refreshTokeRepository;

    public void save(Long memberId, String refreshToken) {

        Optional<RefreshToken> findToken = refreshTokeRepository.findByMemberId(memberId);

        if (findToken.isPresent()) {

            RefreshToken token = findToken.get();
            token.updateToken(refreshToken);
            return;
        }

        refreshTokeRepository.save(RefreshToken.builder()
                .memberId(memberId)
                .token(refreshToken)
                .build());
    }

    public RefreshToken findRefreshToken(Long memberId) {
        return refreshTokeRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NoSuchElementException("리프래시 토큰을 찾을수 없습니다"));
    }

    public Cookie findRefreshTokenCookieByName(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter((c) -> c.getName().equals("refreshToken"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("쿠키에 리프레시 토큰이 없습니다."));
    }
}
