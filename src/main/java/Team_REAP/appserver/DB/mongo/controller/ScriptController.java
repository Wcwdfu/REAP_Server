package Team_REAP.appserver.DB.mongo.controller;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.dto.ModifiedScriptDto;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    // Create
    @PostMapping("/test/mongo")
    public ResponseEntity<Script> createScript(@RequestBody Script script) {
        Script createdScript = scriptService.createScript(script);
        return ResponseEntity.ok(createdScript);
    }

    // Read by ID
    @GetMapping("/test/mongo/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable String id) {
        Optional<Script> script = scriptService.getScriptById(id);
        return script.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/test/mongo/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable String id) {
        //scriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }


    // recordName과 topic 동시에 업데이트
    @Operation(summary = "음성 데이터 - 제목, 주제 업데이트",
            description = "클라이언트가 스크립트 제목과 주제를 업데이트 할 수 있습니다.")
    @PutMapping("/auth/script")
    public ResponseEntity<ModifiedScriptDto> updateRecordNameAndTopic(
            @AuthUser String userId,
            @RequestParam("scriptId") String scriptId,
            @RequestParam("newName") String newRecordName,
            @RequestParam("newTopic") String newTopic) {

        Script updatedScript = scriptService.updateRecordNameAndTopic(userId, scriptId, newRecordName, newTopic);

        if (updatedScript == null) {
            return ResponseEntity.notFound().build();
        }

        ModifiedScriptDto modifiedScriptDto = new ModifiedScriptDto(newRecordName, newTopic);

        return ResponseEntity.status(HttpStatus.OK).body(modifiedScriptDto);
    }
}
