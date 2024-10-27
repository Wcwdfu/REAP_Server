package Team_REAP.appserver.common.login.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Map;

@RequiredArgsConstructor
public class OAuth2UserRequest {

    private final ClientRegistration clientRegistration;
    private final OAuth2AccessToken accessToken;
    private final Map<String, Object> additionalParameters;
}
