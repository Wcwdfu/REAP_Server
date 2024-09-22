package Team_REAP.appserver.Controller;

import Team_REAP.appserver.Service.STTService;
import Team_REAP.appserver.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class STTController {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final STTService sttService;


    @PostMapping("/recognize-url")
    public ResponseEntity<String> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @RequestParam("date") String date,
                                                        @RequestParam("language") String language,
                                                        @RequestParam(value = "completion", required = false, defaultValue = "sync") String completion, // default가 async이다.
                                                        @RequestParam(value = "callback", required = false) String callback,
                                                        @RequestParam(value = "wordAlignment", required = false, defaultValue = "true") boolean wordAlignment,
                                                        @RequestParam(value = "fullText", required = false, defaultValue = "true") boolean fullText,
                                                        @RequestParam(value = "resultToObs", required = false, defaultValue = "false") boolean resultToObs,
                                                        @RequestParam(value = "noiseFiltering", required = false, defaultValue = "true") boolean noiseFiltering) {

        return sttService.audioToText(media, date, language, completion, callback, wordAlignment, fullText, resultToObs, noiseFiltering);
    }
}
