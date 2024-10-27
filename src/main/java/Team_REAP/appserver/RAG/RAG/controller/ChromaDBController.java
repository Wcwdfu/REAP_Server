package Team_REAP.appserver.RAG.RAG.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChromaDBController {

    @Value("classpath:testData.txt")
    Resource resource;
    VectorStore vectorStore;

    public ChromaDBController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Operation(
            summary = "테스트용 :: 테스트 데이터 백터스토어에 넣기",
            description = "테스트 데이터를 크로마db에 저장합니다.")
    @GetMapping("/test/load")
    public String load() throws IOException{
        List<Document> documents = Files.lines(resource.getFile().toPath())
                .map(Document::new)
                .toList();
        TextSplitter textSplitter=new TokenTextSplitter();
        for (Document document : documents) {
            List<Document> splitteddocs = textSplitter.split(document);
            System.out.println("before adding document: " + document.getContent());
            vectorStore.add(splitteddocs);
            System.out.println("Added document: "+ document.getContent());
        }
        return "Loaded " + resource.getFilename();
    }

    @Operation(
            summary = "테스트용 :: 크로마db 유사도 검색",
            description = "내용없음")
    @GetMapping("/test/search")
    public String search(){
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query("classic novel about wealth and society").withTopK(3));
        return results.toString();
    }
}
