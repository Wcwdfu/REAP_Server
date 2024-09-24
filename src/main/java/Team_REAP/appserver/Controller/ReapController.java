package Team_REAP.appserver.Controller;


import Team_REAP.appserver.Entity.User;
import Team_REAP.appserver.Repository.UserRepository;
import Team_REAP.appserver.Service.ReapService;
import Team_REAP.appserver.Service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/user")
public class ReapController {
    private final UserService userService;
    private final ReapService reapService;

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private int id;
    private String date;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{name}/{date}/{time}/{text}")
    public String create(@PathVariable String name, @PathVariable String date, @PathVariable String time, @PathVariable String text) {
        return userService.create(name, date, time, text);
    }

    @GetMapping("")
    public User read(String id) {

        return userService.read(id);
    }

    @GetMapping("/{name}/{date}") // 이름으로 사용자 읽기
    public String readByNameAndDate(@PathVariable String name, @PathVariable String date, @RequestParam("prompt") String prompt) {

        return reapService.questionAndAnswering(name, date, prompt);
    }

    @PutMapping("")
    public User update(String id, String name) {
        return userService.update(id, name);
    }

    @DeleteMapping("")
    public void delete(String id) {
        userService.delete(id);
    }

}
