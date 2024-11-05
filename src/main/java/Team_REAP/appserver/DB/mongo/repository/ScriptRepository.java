package Team_REAP.appserver.DB.mongo.repository;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ScriptRepository extends MongoRepository<Script, String> {
    List<Script> findByUserIdOrderByUploadedDateDescUploadedTimeDesc(String userId);
    List<Script> findByUserIdAndRecordedDateOrderByUploadedDateDescUploadedTimeDesc(String userId, String recordedDate);
    Script findFirstByUserIdAndRecordedDateAndRecordName(String userId, String recordedDate, String recordName);

    Optional<Script> findByRecordIdAndUserId(String recordId, String userId);
}

