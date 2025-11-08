package woowacourse.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.chatting.domain.RefreshToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokeRepository extends JpaRepository<RefreshToken, Long> {

    @Transactional
    void deleteByMemberId(Long memberId);

    @Transactional
    Optional<RefreshToken> findByMemberId(Long memberId);
}
