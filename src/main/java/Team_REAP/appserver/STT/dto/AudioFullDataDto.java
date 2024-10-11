package Team_REAP.appserver.STT.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AudioFullDataDto {

    private String userId; // 유저 식별
    private String recordName; // 파일 이름
    private String recordedDate; // 녹음한 날짜
    private String uploadedDate; // 업로드된 날짜
    private String uploadedTime; // 업로드된 시간
    private String topic; // 주제
    private String text; // 변환한 텍스트


}
