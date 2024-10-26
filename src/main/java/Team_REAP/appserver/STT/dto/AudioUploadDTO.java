package Team_REAP.appserver.STT.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AudioUploadDTO {

    private String fileName;
    private String s3Url;
}
