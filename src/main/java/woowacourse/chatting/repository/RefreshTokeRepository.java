package woowacourse.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import woowacourse.chatting.domain.RefreshToken;

@Repository
public interface RefreshTokeRepository extends JpaRepository<RefreshToken, Long> {

}
