package Team_REAP.appserver.common.login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/common/login")
public class LoginController {

    @GetMapping("/kakao")
    public RedirectView kakaoLogin() {
        // 카카오 로그인 처리 로직
        return new RedirectView("/api/common/login/info");
    }
}
