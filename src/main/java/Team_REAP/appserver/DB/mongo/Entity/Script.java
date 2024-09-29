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
@Document(collection = "members")
public class Script {
    @Id
    private String id;
    private String recordId; // 녹음 파일 번호
    private String date;
    private String text;
}
