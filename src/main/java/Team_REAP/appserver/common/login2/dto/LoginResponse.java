package Team_REAP.appserver.common.login2.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String jwtToken;
}
