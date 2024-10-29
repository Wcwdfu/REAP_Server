package Team_REAP.appserver.DB.mongo.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScriptService {
    private final ScriptRepository scriptRepository;

    public Script saveScript(Script script) {
        return scriptRepository.save(script);
    }

    public List<Script> findRecentScriptsByUserId(String userId) {
        List<Script> scripts = scriptRepository.findByUserIdOrderByUploadedDateDescUploadedTimeDesc(userId);
        if (scripts.isEmpty()) {
            log.info("No scripts found for userId: {}", userId);
        }
        return scripts;
    }

    public List<Script> findScriptsByUserIdAndRecordedDate(String userId, String recordedDate) {
        List<Script> scripts = scriptRepository.findByUserIdAndRecordedDateOrderByUploadedDateDescUploadedTimeDesc(userId, recordedDate);
        if (scripts.isEmpty()) {
            log.info("No scripts found for userId: {} and recordedDate: {}", userId, recordedDate);
        }
        return scripts;
    }

    public Script findScriptByUserIdAndRecordedDateAndRecordName(String userId, String recordedDate, String recordName) {
        Script script = scriptRepository.findFirstByUserIdAndRecordedDateAndRecordName(userId, recordedDate, recordName);
        if (script == null) {
            log.info("No script found for userId: {}, recordedDate: {}, recordName: {}", userId, recordedDate, recordName);
        }
        return script;
    }

    //연동 확인을 위한 코드

    // Create
    public Script createScript(Script script) {
        return scriptRepository.save(script);
    }

    // Read by ID
    public Optional<Script> getScriptById(String id) {
        return scriptRepository.findById(id);
    }
    // Delete
    public void deleteScript(String id) {
        scriptRepository.deleteById(id);
    }
}
