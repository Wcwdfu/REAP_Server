package Team_REAP.appserver.DB.mongo.repository;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScriptRepository extends MongoRepository<Script, String> {
    List<Script> findByUserIdOrderByUploadedDateDescUploadedTimeDesc(String userId);
    List<Script> findByUserIdAndRecordedDateOrderByUploadedDateDescUploadedTimeDesc(String userId, String recordedDate);
    Script findFirstByUserIdAndRecordedDateAndRecordName(String userId, String recordedDate, String recordName);
}

