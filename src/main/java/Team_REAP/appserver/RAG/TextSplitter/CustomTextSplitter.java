package Team_REAP.appserver.RAG.TextSplitter;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "슬라이딩 윈도우" 방식으로 토큰 분할을 수행하는 예시 클래스.
 * TextSplitter를 상속받으므로, apply() / split() / split(Document) 등 상위 로직과 연동 가능.
 */
public class CustomTextSplitter extends TextSplitter {

    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
    private final Encoding encoding;

    /** 청크 하나에 포함될 최대 토큰 수 */
    private final int chunkSize;
    /** 청크 간 오버랩(겹치는) 토큰 수 */
    private final int chunkOverlap;
    /** 최대 몇 개의 청크까지 만들 것인지 (제한 없으면 매우 큰 값) */
    private final int maxNumChunks;

    /**
     * @param chunkSize     한 청크의 최대 토큰 수
     * @param chunkOverlap  바로 다음 청크와 몇 개 토큰을 겹칠지
     * @param maxNumChunks  (선택) 분할할 청크의 최대 개수 제한
     */
    public CustomTextSplitter(int chunkSize, int chunkOverlap, int maxNumChunks) {
        // TextSplitter 부모 생성자 호출(디폴트)
        super();

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkOverlap must be smaller than chunkSize");
        }
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.maxNumChunks = maxNumChunks > 0 ? maxNumChunks : Integer.MAX_VALUE;

        // Spring AI에서 기본으로 쓰이는 cl100k_base 인코딩 사용
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    /**
     * TextSplitter가 요구하는 추상 메서드.
     * 한 문서(단일 text)에 대한 분할 로직을 여기서 구현한다.
     */
    @Override
    protected List<String> splitText(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        // 1) 문자열 -> 토큰 리스트
        List<Integer> allTokens = getEncodedTokens(text);

        // 2) 슬라이딩 윈도우 분할
        List<List<Integer>> tokenChunks = slidingWindowTokens(allTokens, chunkSize, chunkOverlap, maxNumChunks);

        // 3) 각 토큰 청크를 다시 문자열로 디코딩하여 결과 리스트 반환
        List<String> result = new ArrayList<>();
        for (List<Integer> chunk : tokenChunks) {
            String chunkText = decodeTokens(chunk);
            if (!chunkText.isBlank()) {
                result.add(chunkText.trim());
            }
        }

        return result;
    }

    // --------------------------------------------------------------------------------------
    // 아래는 "TokenTextSplitter"에 있던 private 메서드를 여기서 그대로(또는 비슷하게) 재구현한 것.
    // --------------------------------------------------------------------------------------

    private List<Integer> getEncodedTokens(String text) {
        Assert.notNull(text, "Text must not be null");
        // jtokkit 라이브러리의 encode()가 IntArrayList(int[]) 형태를 반환 -> .boxed()로 List<Integer> 얻기
        return this.encoding.encode(text).boxed();
    }

    private String decodeTokens(List<Integer> tokens) {
        Assert.notNull(tokens, "Tokens must not be null");
        IntArrayList tokensIntArray = new IntArrayList(tokens.size());
        Objects.requireNonNull(tokensIntArray);
        tokens.forEach(tokensIntArray::add);

        return this.encoding.decode(tokensIntArray);
    }

    /**
     * "chunkSize, chunkOverlap"을 적용해 실제로 토큰 리스트를 슬라이딩 윈도우 방식으로 자르는 핵심 로직.
     * 예) chunkSize=100, chunkOverlap=20 => step=80씩 start 인덱스를 이동
     */

    private List<List<Integer>> slidingWindowTokens(
            List<Integer> tokens,
            int chunkSize,
            int chunkOverlap,
            int maxNumChunks
    ) {
        List<List<Integer>> result = new ArrayList<>();
        int step = chunkSize - chunkOverlap;

        int start = 0;
        int count = 0;

        while (start < tokens.size() && count < maxNumChunks) {
            int end = Math.min(start + chunkSize, tokens.size());

            // subList()는 View이므로, new ArrayList로 복사
            List<Integer> window = new ArrayList<>(tokens.subList(start, end));
            result.add(window);

            count++;
            if (end >= tokens.size()) {
                break;
            }
            start += step;
        }

        return result;
    }
}
