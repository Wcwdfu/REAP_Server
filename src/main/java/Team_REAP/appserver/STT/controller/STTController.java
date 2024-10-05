package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.STT.service.STTService;
import Team_REAP.appserver.DB.mongo.service.MongoUserService;
import Team_REAP.appserver.DB.mongo.Entity.Script;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
public class STTController {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final STTService sttService;

    private final MongoUserService mongoUserService;


    @Operation(summary = "음성 -> S3저장, 스크립트 변환 후 DB 저장")
    @PostMapping("/recognize-url")
    public ResponseEntity<Object> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @RequestParam("user") String userName,
                                                        @RequestParam("topic") String topic) throws IOException {

        ResponseEntity<Object> response = sttService.audioToText(media, userName, topic);
        return response;
    }

    @GetMapping("/api/detail/{userid}/record-script") // 임시로 mongoDb에서 Record를 가져오도록 만들었다.
    public ResponseEntity<Object> showAudioScript(@PathVariable String userid,
                                                  @RequestParam("id") String id){

        // TODO : userid 등등의 뭔가를 가져와서 mongodb 객체 id를 찾을 수 있도록 해야함

        Script script = mongoUserService.findById(id, Script.class, "record");

        return ResponseEntity.status(HttpStatus.OK).body(script);
    }



}
