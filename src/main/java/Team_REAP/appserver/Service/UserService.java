package Team_REAP.appserver.Service;

import Team_REAP.appserver.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final MongoTemplate mongoTemplate;

    public String create(String name) {
        User user = User.builder()
                .name(name)
                .build();

        return mongoTemplate.insert(user, "members").get_id();
    }

    public User read(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        User user = mongoTemplate.findOne(query, User.class, "members");
        if (user == null) {
            System.out.println("No user found with id: " + id);
        }

        return mongoTemplate.findOne(query, User.class, "members");
    }

    public User update(String id, String name) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("name", name);

        return mongoTemplate.findAndModify(query, update, User.class, "members");
    }

    public void delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));

        mongoTemplate.remove(query, User.class, "members");
    }
}
