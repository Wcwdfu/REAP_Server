package Team_REAP.appserver.AI;

import Team_REAP.appserver.AI.model.Answer;
import Team_REAP.appserver.AI.model.Question;

public interface OpenAIService {
    String getAnswer(String question);

    Answer getAnswer(Question question);

}
