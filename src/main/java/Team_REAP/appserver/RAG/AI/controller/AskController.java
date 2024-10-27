package Team_REAP.appserver.RAG.AI.controller;

import Team_REAP.appserver.RAG.AI.model.Answer;
import Team_REAP.appserver.RAG.AI.model.Question;
import Team_REAP.appserver.RAG.AI.service.ChatService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AskController {

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
    public Answer Ask(
            @AuthUser String userid,
            @RequestBody Question question
    ) {

        return chatService.generateChatResponse(question);
    }

}
