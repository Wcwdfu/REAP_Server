package Team_REAP.appserver.common.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TestTokenDto {

    private String accessToken;

    private String refreshToken;
}
