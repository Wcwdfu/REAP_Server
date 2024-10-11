package Team_REAP.appserver.common.login.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate // Entity update시, 원하는 데이터만 update하기 위함
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username; // 로그인한 사용자의 이름

    @Column(name = "email", nullable = false)
    private String email; // 로그인한 사용자의 이메일

    @Column(name = "provider", nullable = false)
    private String provider; // 사용자가 로그인한 서비스(ex) google, naver..)

    // 사용자의 이름이나 이메일을 업데이트하는 메소드
    public User updateUser(String username, String email) {
        this.username = username;
        this.email = email;

        return this;
    }
}