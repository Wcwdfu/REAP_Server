package Team_REAP.appserver.common.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String refreshToken; // 클라이언트에서 보내는 리프레시 토큰
}
