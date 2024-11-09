package Team_REAP.appserver.RAG.AI.service;

import Team_REAP.appserver.DB.chroma.service.ChromaDBService;
import Team_REAP.appserver.RAG.AI.model.Answer;
import Team_REAP.appserver.RAG.AI.model.Question;
import Team_REAP.appserver.RAG.AI.model.StreamAnswer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    //todo 이거 왜 null로 뜨냐
//    @Value("classpath:templates/defaultPrompt.st")
//    private Resource defaultTemplate;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.defaultSystem(
                "다음과 같은 맥락을 사용하여 질문에 대답하세요." +
                "만약 답을 모르면 모른다고만 말하고 답을 지어내려고 하지 마세요." +
                "답변은 최대 4문장으로 대답하되, 가능한 간결하게 유지하세요.").build();
        this.vectorStore=vectorStore;
    }

    public Answer generateChatResponse(Question question){
        //답변이 다 완성되고 난 후 전달하기
        String answer=chatClient.prompt()
                .user(question.question())
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .content();

        return new Answer(answer);
    }

    public Answer generateChatResponse2(String userId, Question question) {
        // 1. 사용자별 문서 검색
        List<Document> userDocuments = searchUserDocuments(userId, question.question());

        // 2. 검색된 문서를 프롬프트에 추가
        String context = userDocuments.stream()
                .map(Document::getContent) // 문서 내용 가져오기
                .collect(Collectors.joining("\n---\n")); // 문서를 구분하여 결합

        // 3. ChatClient 프롬프트에 맥락 포함하여 대답 생성
        String answer = chatClient.prompt()
                .system("\"다음과 같은 맥락을 사용하여 질문에 대답하세요.\" +\n" +
                        "\"만약 답을 모르면 모른다고만 말하고 답을 지어내려고 하지 마세요.\" +\n" +
                        "\"답변은 최대 4문장으로 대답하되, 가능한 간결하게 유지하세요" + context)
                .user(question.question())
                .advisors(new QuestionAnswerAdvisor(vectorStore)) // 사용자별로 저장된 문서를 참조하여 응답 생성
                .call()
                .content();

        return new Answer(answer);
    }


    public StreamAnswer generateStreamChatResponse(Question question) {
        //답변을 실시간으로 전달하기
        Flux<String> answer=chatClient.prompt()
                .user(question.question())
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .stream()
                .content();

        return new StreamAnswer(answer);
    }


    public List<Document> searchUserDocuments(String userId, String query) {
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(3)
                .withFilterExpression("userid == '" + userId + "'");

        return vectorStore.similaritySearch(searchRequest);
    }

}
