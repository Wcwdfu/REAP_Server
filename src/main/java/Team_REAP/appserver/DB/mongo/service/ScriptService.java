package Team_REAP.appserver.DB.mongo.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.repository.ScriptRepository;
import Team_REAP.appserver.STT.dto.ScriptTextDataDTO;
import Team_REAP.appserver.DB.S3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScriptService {
    private final ScriptRepository scriptRepository;
    private final S3Service s3Service;

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

    public List<ScriptTextDataDTO> getFormattedAudioData(String userid, String recordedDate, String recordName) {
        Script script = findScriptByUserIdAndRecordedDateAndRecordName(userid, recordedDate, recordName);

        List<ScriptTextDataDTO> formattedScripts = Arrays.stream(script.getText().split("\n"))
                .map(line -> {
                    String[] parts = line.trim().split(" ", 4); // [timestamp, elapsedTime, speaker, text]
                    return new ScriptTextDataDTO(
                            parts.length > 0 ? parts[0] : "",    // timestamp
                            parts.length > 1 ? parts[1] : "",    // elapsedTime
                            parts.length > 2 ? parts[2] : "",    // speaker
                            parts.length > 3 ? parts[3] : ""     // text
                    );
                })
                .collect(Collectors.toList());

        return formattedScripts;
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
//    public void deleteScript(String id) {
//        scriptRepository.deleteById(id);
//    }

    public Script updateRecordNameAndTopic(String userId, String scriptId, String newRecordName, String newTopic) {
        Optional<Script> optionalScript = scriptRepository.findByRecordIdAndUserId(scriptId, userId);

        if (optionalScript.isEmpty()) {
            log.info("No script found for userId: {}, scriptId: {}", userId, scriptId);
            return null;
        }

        Script script = optionalScript.get();
        String oldRecordName = script.getRecordName();
        String recordedDate = script.getRecordedDate();

        newRecordName = appendExtensionIfMissing(newRecordName, oldRecordName);


        // S3에서 파일 이름 변경
        s3Service.moveFile(userId, recordedDate, oldRecordName, newRecordName);

        // MongoDB에서 recordName과 topic 업데이트
        script.setRecordName(newRecordName);
        script.setTopic(newTopic);
        return scriptRepository.save(script);
    }

    @NotNull
    private static String appendExtensionIfMissing(String newRecordName, String oldRecordName) {
        // newRecordName에 확장자가 없으면 oldRecordName의 확장자를 붙임
        if (!newRecordName.contains(".")) {
            // oldRecordName에서 확장자 추출
            String extension = "";
            int dotIndex = oldRecordName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < oldRecordName.length() - 1) {
                extension = oldRecordName.substring(dotIndex);
            }
            newRecordName += extension;
        }
        return newRecordName;
    }


    public void deleteScript(String userId, String recordId) {
        Script script = scriptRepository.findByRecordIdAndUserId(recordId, userId)
                .orElseThrow(() -> new NoSuchElementException("Record not found for userId: " + userId + ", recordId: " + recordId));

        scriptRepository.delete(script);
    }

    public boolean isRecordIdDuplicate(String recordId) {
        return scriptRepository.findByRecordId(recordId).isPresent();
    }
}
