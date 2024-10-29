package Team_REAP.appserver.STT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScriptTextDataDTO {
    private String timestamp;
    private String elapsedTime;
    private String speaker;
    private String text;
}
