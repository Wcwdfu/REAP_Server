package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.STT.dto.AudioUploadDTO;
import Team_REAP.appserver.STT.service.AudioService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class STTController {

    private final AudioService audioService;


    @Operation(summary = "음성 -> S3저장, 스크립트 변환 후 DB 저장", description = "사용자로부터 음성을 받으면 네이버 STT를 통해서 음성을 텍스트로 변환합니다." +
            "STT 결과를 가지고 스크립트로 만들어 음성 파일은 S3에 저장하고 스크립트 관련 내용은 MongoDb에 저장합니다.")
    @PostMapping("/auth/upload")
    public ResponseEntity<Object> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @AuthUser String userId,
                                                        @RequestParam("topic") String topic) throws IOException {


        try {
            AudioUploadDTO audioUploadDTO = audioService.processAudio(media, userId, topic);
            return ResponseEntity.status(HttpStatus.OK).body(audioUploadDTO);
        } catch (Exception e) {
            log.error("Error processing audio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}
