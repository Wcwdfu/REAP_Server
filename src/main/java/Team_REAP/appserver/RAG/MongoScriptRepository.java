package Team_REAP.appserver.RAG;

import Team_REAP.appserver.Deprecated.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoScriptRepository extends MongoRepository<User, String> {
    //몽고DB꺼

    User findByName(String name);
}

