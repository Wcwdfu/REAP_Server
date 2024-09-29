package Team_REAP.appserver.RAG;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "record")
public class model {

    @Id
    private String id;
    private String text;
    private String recordId;
    private String date;
}
