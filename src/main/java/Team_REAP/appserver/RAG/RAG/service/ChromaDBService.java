package Team_REAP.appserver.RAG.RAG.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaDBService {
    private final VectorStore vectorStore;

    public void addScriptToVectorStore(Script script) {
        TextSplitter textSplitter = new TokenTextSplitter();

        String content = script.getText();
        if (content != null && !content.isEmpty()) {
            Document document = new Document(content);
            List<Document> splitDocs = textSplitter.split(document);
            vectorStore.add(splitDocs);
            log.info("Added script with ID: {}", script.getId());
        } else {
            log.warn("Script with ID {} has empty content.", script.getId());
        }
    }
}
