package Team_REAP.appserver.RAG.AI.model;

import reactor.core.publisher.Flux;

public record StreamAnswer(Flux<String> answer) {
}
