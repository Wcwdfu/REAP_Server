package Team_REAP.appserver.RAG.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.repository.ScriptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {
    private ScriptRepository scriptRepository;
    private List<Script> scripts;
}
