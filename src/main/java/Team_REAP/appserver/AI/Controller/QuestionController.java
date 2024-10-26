//package Team_REAP.appserver.AI.Controller;
//
//import Team_REAP.appserver.AI.OpenAIService;
//import Team_REAP.appserver.AI.model.Answer;
//import Team_REAP.appserver.AI.model.Question;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class QuestionController {
//
//    private final OpenAIService openAIService;
//
//    public QuestionController(OpenAIService openAIService) {
//        this.openAIService = openAIService;
//    }
//
//    @PostMapping("/ask")
//    public Answer askQuestion(@RequestBody Question question){
//        return openAIService.getAnswer(question);
//    }
//}
