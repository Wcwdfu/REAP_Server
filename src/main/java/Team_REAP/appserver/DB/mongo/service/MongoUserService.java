package Team_REAP.appserver.DB.mongo.service;

import Team_REAP.appserver.DB.mongo.Entity.Record;
import Team_REAP.appserver.Deprecated.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class MongoUserService {
    private final MongoTemplate mongoTemplate;

    public String create(String name, String date,String time,String text) { // 곧 안 쓸 예정
        User user = User.builder()
                .name(name)
                .date(date)
                .time(time)
                .text(text)
                .build();

        return mongoTemplate.insert(user, "user").getId();
    }

    public String createAll(String recordId, String date, String text) { // 녹음 파일 스크립트로 만든 것 저장

        Record record = Record.builder()
                .recordId(recordId)
                .date(date)
                .text(text)
                .build();

        return mongoTemplate.insert(record, "record").getId();
    }

    public <T> T findById(String id, Class<T> clazz, String collectionName) {
        Query query = new Query(Criteria.where("_id").is(id));

        T result = mongoTemplate.findOne(query, clazz, collectionName);
        if (result == null) {
            log.info("No result found with id: " + id);
        }

        return result;

        //return mongoTemplate.findOne(query, User.class, "members");
    }

    public List<User> readByName(@PathVariable String name) {
        // 쿼리 생성: name이 해당 이름인 데이터 검색
        Query query = new Query(Criteria.where("name").is(name));

        // MongoDB에서 데이터 가져오기
        List<User> users = mongoTemplate.find(query, User.class, "user");
        for (User user : users) {
            System.out.println(user.getId());
            System.out.println(user.getName());
        }

        return users;
    }

    public List<User> readByNameAndDate(@PathVariable String name, @PathVariable String date) {
        // 쿼리 생성: name이 해당 이름인 데이터 검색
        Query query = new Query(Criteria.where("name").is(name).and("date").is(date));

        // MongoDB에서 데이터 가져오기
        List<User> users = mongoTemplate.find(query, User.class, "user");
        for (User user : users) {
            System.out.println(user.getId());
            System.out.println(user.getName());
        }

        return users;
    }

    public User update(String id, String name) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("name", name);

        return mongoTemplate.findAndModify(query, update, User.class, "users");
    }

    public void delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));

        mongoTemplate.remove(query, User.class, "members");
    }


}
