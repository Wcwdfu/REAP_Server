package Team_REAP.appserver.common.login.service;

import Team_REAP.appserver.common.login.domain.User;
import Team_REAP.appserver.common.login.domain.OAuthAttributes;
import Team_REAP.appserver.common.login.repository.JpaUserRepository;
import Team_REAP.appserver.common.login.dto.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 로그인을 수행한 서비스의 이름

        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName(); // PK가 되는 정보

        Map<String, Object> attributes = oAuth2User.getAttributes(); // 사용자가 가지고 있는 정보

        UserProfile loginProfile = OAuthAttributes.extract(registrationId, attributes);
        loginProfile.setProvider(registrationId);

        updateOrSaveUser(loginProfile);

        Map<String, Object> customAttribute =
                getCustomAttribute(registrationId, userNameAttributeName, attributes, loginProfile);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                customAttribute,
                userNameAttributeName);
    }

    public Map getCustomAttribute(String registrationId,
                                  String userNameAttributeName,
                                  Map<String, Object> attributes,
                                  UserProfile loginProfile) {
        Map<String, Object> customAttribute = new ConcurrentHashMap<>();

        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("provider", registrationId);
        customAttribute.put("name", loginProfile.getUsername());
        customAttribute.put("email", loginProfile.getEmail());

        return customAttribute;
    }

    public User updateOrSaveUser(UserProfile loginProfile) {
        User user = jpaUserRepository
                .findUserByEmailAndProvider(loginProfile.getEmail(), loginProfile.getProvider())
                .map(value -> value.updateUser(loginProfile.getUsername(), loginProfile.getEmail()))
                .orElse(loginProfile.toEntity());

        return jpaUserRepository.save(user);
    }
}