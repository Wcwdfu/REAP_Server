package Team_REAP.appserver.DB.Chroma.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.RAG.TextSplitter.CustomTokenTextSplitter;
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

    public void addScriptToVectorStore(Script script,String userId) {
        CustomTokenTextSplitter splitter = new CustomTokenTextSplitter(
                1000, // chunkSize
                200,  // chunkOverlap
                true, // keepSeparator
                350,  // minChunkSizeChars
                5,    // minChunkLengthToEmbed
                100 // maxNumChunks
        );
//        TextSplitter textSplitter = new TokenTextSplitter();

        String content = script.getText();
        if (content != null && !content.isEmpty()) {
            Document document = Document.builder()
                    .withMetadata("userid",userId).build();

            List<Document> splitDocs = splitter.split(List.of(document));
            vectorStore.add(splitDocs);
            log.info("Added script with ID: {}", script.getId());
        } else {
            log.warn("Script with ID {} has empty content.", script.getId());
        }
    }
}
