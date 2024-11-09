package Team_REAP.appserver.RAG.TextSplitter;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomTokenTextSplitter {
    private final TokenTextSplitter tokenTextSplitter;
    private final int chunkOverlap;

    public CustomTokenTextSplitter(int chunkSize, int chunkOverlap, boolean keepSeparator, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks) {
        this.tokenTextSplitter = new TokenTextSplitter(chunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator);
        this.chunkOverlap = chunkOverlap;
    }

    // `split` 메서드를 추가하여 `TokenTextSplitter`의 split 기능을 노출
    public List<Document> split(List<Document> documents) {
        List<Document> splitDocs = new ArrayList<>();

        for (Document document : documents) {
            List<String> chunks = splitTextWithOverlap(document.getContent());
            for (String chunk : chunks) {
                splitDocs.add(new Document(chunk, document.getMetadata()));
            }
        }

        return splitDocs;
    }

    // 오버랩을 적용한 텍스트 분할 메서드
    public List<String> splitTextWithOverlap(String text) {
        List<String> chunks = new ArrayList<>();
        List<String> originalChunks = tokenTextSplitter.split(List.of(new Document(text))).stream()
                .map(Document::getContent)
                .toList();

        for (int i = 0; i < originalChunks.size(); i++) {
            String currentChunk = originalChunks.get(i);
            chunks.add(currentChunk);

            if (i < originalChunks.size() - 1 && chunkOverlap > 0) {
                String nextChunk = originalChunks.get(i + 1);
                int overlapIndex = Math.min(currentChunk.length(), chunkOverlap);
                chunks.add(currentChunk.substring(0, overlapIndex) + nextChunk);
            }
        }

        return chunks;
    }
}
