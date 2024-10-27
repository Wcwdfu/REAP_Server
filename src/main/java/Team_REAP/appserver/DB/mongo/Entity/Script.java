package Team_REAP.appserver.DB.mongo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "record")
public class Script {
    @Id
    private String id;
    private String recordId; // 녹음 파일 번호
    private String userId; // 유저 식별
    private String recordName; // 파일 이름
    private String recordedDate; // 녹음한 날짜
    private String uploadedDate; // 업로드된 날짜
    private String uploadedTime; // 업로드된 시간
    private String topic; // 주제
    private String text; // 변환한 텍스트
}
