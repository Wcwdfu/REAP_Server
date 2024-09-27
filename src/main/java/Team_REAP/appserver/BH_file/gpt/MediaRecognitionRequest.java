package Team_REAP.appserver.BH_file.gpt;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class MediaRecognitionRequest {
    private String url;
    private String language;
    private String completion;
    private String callback;
    private Map<String, Object> userdata;
    private boolean wordAlignment;
    private boolean fullText;
    private boolean resultToObs;
    private boolean noiseFiltering;
    private List<Boosting> boostings;
    private boolean useDomainBoostings;
    private String forbiddens;
    private Diarization diarization;
    private Sed sed;
    private String format;

    // Getters and Setters

    public static class Boosting {
        private String words;

        // Getters and Setters
    }

    public static class Diarization {
        private boolean enable;

        // Getters and Setters
    }

    public static class Sed {
        private boolean enable;

        // Getters and Setters
    }
}