package Team_REAP.appserver.Repository;

import Team_REAP.appserver.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByName(String name);
}

