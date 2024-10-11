package Team_REAP.appserver.common.login.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail; // 유저 이메일

    @Column(nullable = false)
    private String token; // 리프레시 토큰

    public RefreshToken(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
    }
}
