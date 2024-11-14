package Team_REAP.appserver.DB.mySQL.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Getter
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer memberNo;

    private String kakaoId;

    private String nickname;

    //private String email;

    private String userRole;

    private String loginApi;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Member(String kakaoId, String nickname, String userRole, String loginApi, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        //this.email = email;
        this.userRole = userRole;
        this.loginApi = loginApi;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
