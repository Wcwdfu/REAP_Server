package Team_REAP.appserver.Deprecated;


import Team_REAP.appserver.DB.mongo.repository.ScriptRepository;
import Team_REAP.appserver.DB.mongo.service.MongoUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/user")
public class ReapController { // gpt한테 뭔가를 물어보면 대답해주는데 쓰는 컨트롤러
    private final MongoUserService mongoUserService;
    private final ReapService reapService;

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private int id;
    private String date;

    @Autowired
    private ScriptRepository scriptRepository;

    @Operation(summary = "현재 미사용")
    @PostMapping("/{name}/{date}/{time}/{text}")
    public String create(@PathVariable String name, @PathVariable String date, @PathVariable String time, @PathVariable String text) {
        return mongoUserService.create(name, date, time, text);
    }

    @Operation(summary = "현재 미사용")
    @GetMapping("/{name}/{date}") // 이름으로 사용자 읽기
    public String readByNameAndDate(@PathVariable String name, @PathVariable String date, @RequestParam("prompt") String prompt) {

        return reapService.questionAndAnswering(name, date, prompt);
    }

    @Operation(summary = "현재 미사용")
    @PutMapping("")
    public User update(String id, String name) {
        return mongoUserService.update(id, name);
    }

    @Operation(summary = "현재 미사용")
    @DeleteMapping("")
    public void delete(String id) {
        mongoUserService.delete(id);
    }

}
