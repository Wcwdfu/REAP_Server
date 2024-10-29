package Team_REAP.appserver.DB.mongo.controller;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test/MG")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    // Create
    @PostMapping
    public ResponseEntity<Script> createScript(@RequestBody Script script) {
        Script createdScript = scriptService.createScript(script);
        return ResponseEntity.ok(createdScript);
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptById(@PathVariable String id) {
        Optional<Script> script = scriptService.getScriptById(id);
        return script.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable String id) {
        scriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }
}
