package Team_REAP.appserver.AI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenAiServiceImpleTest {

    @Autowired
    OpenAIService openAIService;

    @Test
    void getAnswer() {
        String answer= openAIService.getAnswer("who are you?");
        System.out.println("대답 = " + answer);
    }
}