package Team_REAP.appserver.DB.mongo.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModifiedScriptDto {

    private String modifiedRecordName;
    private String modifiedTopic;
}
