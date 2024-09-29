package Team_REAP.appserver.Deprecated;

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
public class User {
    @Id
    private String id;
    private String recordId; // 녹음 파일 번호
    private String name;
    private String date;
    private String time;
    private String text;
}
