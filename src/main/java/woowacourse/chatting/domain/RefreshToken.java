package woowacourse.chatting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(updatable = false, unique = true)
    private Long memberId;

    private String token;

    public void updateToken(String setToken){
        this.token = setToken;
    }

    @Builder
    public RefreshToken(Long memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }
}
