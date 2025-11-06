package woowacourse.chatting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import woowacourse.chatting.domain.RefreshToken;
import woowacourse.chatting.repository.RefreshTokeRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokeService {

    private final RefreshTokeRepository refreshTokeRepository;

    public void save(Long memberId, String refreshToken) {

        Optional<RefreshToken> findToken = refreshTokeRepository.findById(memberId);

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
}
