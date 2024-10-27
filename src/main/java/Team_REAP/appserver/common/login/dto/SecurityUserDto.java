package Team_REAP.appserver.common.login.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@ToString
@AllArgsConstructor
@Builder
public class SecurityUserDto {
    private String id;
    private String email;
    private String nickname;
    private String picture;
    private String role;
    private Integer memberNo;
}
