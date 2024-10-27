package Team_REAP.appserver.RAG.controller;

import Team_REAP.appserver.AI.model.Answer;
import Team_REAP.appserver.AI.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai/ask")
public class AskController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AskController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore=vectorStore;
    }

    @PostMapping
    public Answer ask(@RequestBody Question question) {
        //답변을 실시간으로 전달하기
//        Flux<String> answer=chatClient.prompt()
//                .user(question.question())
//                .advisors(new QuestionAnswerAdvisor(vectorStore))
//                .stream()
//                .content();

        //답변이 모두 생성될때까지 기다린후 한꺼번에 전달하기
        String answer=chatClient.prompt()
                .user(question.question())
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .content();

        return new Answer(answer);
    }

}
