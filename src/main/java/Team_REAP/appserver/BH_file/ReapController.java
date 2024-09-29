package Team_REAP.appserver.BH_file;


import Team_REAP.appserver.common.user.Entity.User;
import Team_REAP.appserver.common.user.Repository.MongoUserRepository;
import Team_REAP.appserver.BH_file.Service.ReapService;
import Team_REAP.appserver.BH_file.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/user")
public class ReapController { // gpt한테 뭔가를 물어보면 대답해주는데 쓰는 컨트롤러
    private final UserService userService;
    private final ReapService reapService;

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private int id;
    private String date;

    @Autowired
    private MongoUserRepository mongoUserRepository;

    @PostMapping("/{name}/{date}/{time}/{text}")
    public String create(@PathVariable String name, @PathVariable String date, @PathVariable String time, @PathVariable String text) {
        return userService.create(name, date, time, text);
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
