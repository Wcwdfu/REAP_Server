package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.STT.service.STTService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Slf4j
public class STTController {

    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final STTService sttService;

    private final ScriptService mongoUserService;


    @Operation(summary = "음성 -> S3저장, 스크립트 변환 후 DB 저장", description = "사용자로부터 음성을 받으면 네이버 STT를 통해서 음성을 텍스트로 변환합니다." +
            "STT 결과를 가지고 스크립트로 만들어 음성 파일은 S3에 저장하고 스크립트 관련 내용은 MongoDb에 저장합니다.")
    @PostMapping("/auth/uploadMediaFile")
    public ResponseEntity<Object> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @AuthUser String userName,
                                                        @RequestParam("topic") String topic) throws IOException {

        ResponseEntity<Object> response = sttService.audioToText(media, userName, topic);
        return response;
    }
}
