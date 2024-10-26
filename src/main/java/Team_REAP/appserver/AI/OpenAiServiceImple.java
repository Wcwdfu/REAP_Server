package Team_REAP.appserver.AI;

import Team_REAP.appserver.AI.model.Answer;
import Team_REAP.appserver.AI.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OpenAiServiceImple implements OpenAIService {

    private final ChatClient chatClient;

    public OpenAiServiceImple(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getAnswer(String question) {

        return this.chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @Override
    public Answer getAnswer(Question question) {
        return null;
    }
}
