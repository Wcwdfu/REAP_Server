package Team_REAP.appserver.RAG.AI.controller;

import Team_REAP.appserver.RAG.AI.model.Answer;
import Team_REAP.appserver.RAG.AI.model.Question;
import Team_REAP.appserver.RAG.AI.service.ChatService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "테스트용 :: AI에 질문하기",
            description = "질문을 넣어주면 대답을 반환합니다.")
    @PostMapping("/test/ask")
    public Answer testAsk(@RequestBody Question question) {

        return chatService.generateChatResponse(question);
    }

    @Operation(
            summary = "AI에 질문하기",
            description = "질문을 넣어주면 대답을 반환합니다.")
    @PostMapping("/auth/ask")
    public Answer ask(
            @AuthUser String userId,
            @RequestBody Question question
    ) {
        return chatService.generateChatResponse2(userId,question);
    }


    @Operation(
            summary = "테스트용 :: AI에 질문하고, stream으로 대답받기",
            description = "질문을 넣어주면 대답을 반환합니다.")
    @PostMapping(value = "/test/stream/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(
//            @AuthUser String userId,
            @RequestBody Question question
    ) {
        return chatService.generateStreamChatResponse(question).answer();
    }

    @PostMapping(value = "/test/stream", produces = "text/event-stream; charset=UTF-8")
    public Flux<String> testStream() {
        return Flux.just("안녕하세요", "이것은 테스트 스트림입니다")
                .delayElements(Duration.ofSeconds(1));
    }


}
