package Team_REAP.appserver.DB.mongo.repository;

import Team_REAP.appserver.Deprecated.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScriptRepository extends MongoRepository<User, String> {

    User findByName(String name);
}

