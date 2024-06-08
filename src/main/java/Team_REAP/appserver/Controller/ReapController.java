package Team_REAP.appserver.Controller;


import Team_REAP.appserver.Entity.User;
import Team_REAP.appserver.Repository.UserRepository;
import Team_REAP.appserver.Service.UserService;
import Team_REAP.appserver.dto.GPTRequest;
import Team_REAP.appserver.dto.GPTResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/user")
public class ReapController {
    private final UserService userService;

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate;

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

        String condition = "받은 날짜를 0000-00-00 형태로 만들어 줘, 근데 0000은 2024로 바꿔줘";


        // 사용자가 질문한 것을 원하는 데이터로 변환
        GPTRequest pre_request = new GPTRequest(
                model,condition, date, 1,256,1,2,2);

        GPTResponse pre_gptResponse = restTemplate.postForObject(
                apiUrl
                , pre_request
                , GPTResponse.class
        );

        String refinedDate = pre_gptResponse.getChoices().get(0).getMessage().getContent();
        System.out.println(refinedDate);

        // 유저 데이터 찾아서 gpt에 넣기
        List<User> userDatas = userService.readByNameAndDate(name, refinedDate);
        StringBuilder timelog = new StringBuilder();
        for (User data : userDatas) {
            timelog.append(data.getDate()).append(" ");
            timelog.append(data.getTime()).append(" ");
            timelog.append(data.getText()).append("\n");
        }
        String dialog = new String(timelog);

        System.out.println(dialog);

        GPTRequest request = new GPTRequest(
                model,prompt, dialog , 1,256,1,2,2);

        GPTResponse gptResponse = restTemplate.postForObject(
                apiUrl
                , request
                , GPTResponse.class
        );

        return gptResponse.getChoices().get(0).getMessage().getContent();
    }

    @PutMapping("")
    public User update(String id, String name) {
        return userService.update(id, name);
    }

    @DeleteMapping("")
    public void delete(String id) {
        userService.delete(id);
    }

    private void parsing(String content) {
        // JSON 파싱하여 ID와 날짜 추출
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(content);

            this.id = jsonNode.get("ID").asInt();
            this.date = jsonNode.get("date").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
