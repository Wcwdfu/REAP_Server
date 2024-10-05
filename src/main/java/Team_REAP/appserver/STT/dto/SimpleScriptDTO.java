package Team_REAP.appserver.STT.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SimpleScriptDTO {

    private String fileName;
    private String recordedDate;
    private String uploadedDate;
    private String uploadedTime;
    private String topic;
}
