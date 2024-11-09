package Team_REAP.appserver.DB.chroma.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.RAG.TextSplitter.CustomTokenTextSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
                    .withId(script.getRecordId())
                    .withMetadata("userid",userId)
                    .withContent(content) // 문서 내용 추가
                    .build();

            List<Document> splitDocs = splitter.split(List.of(document));
            vectorStore.add(splitDocs);
            log.info("Added script with ID: {}", script.getId());
        } else {
            log.warn("Script with ID {} has empty content.", script.getId());
        }
    }

    //문서 삭제
    public boolean deleteDocuments(List<String> documentIds) {
        // VectorStore의 delete 메서드를 호출하여 문서를 삭제
        Optional<Boolean> result = vectorStore.delete(documentIds);

        // 삭제 성공 여부 반환
        return result.orElse(false);
    }

}
