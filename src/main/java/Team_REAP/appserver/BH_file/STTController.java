package Team_REAP.appserver.BH_file;

import Team_REAP.appserver.BH_file.Service.STTService;
import Team_REAP.appserver.BH_file.Service.dto.AudioUploadDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@Slf4j
public class STTController {
    @Value("${naver.cloud.invoke.url}")
    private String apiUrl;

    @Value("${naver.cloud.secret.key}")
    private String secretKey;

    private final STTService sttService;


    @Operation(summary = "음성 -> S3저장, 스크립트 변환 후 DB 저장")
    @PostMapping("/recognize-url")
    public ResponseEntity<Object> recognizeMediaFromURL(@RequestParam("media") MultipartFile media,
                                                        @RequestParam("user") String userName) throws IOException {

        ResponseEntity<Object> response = sttService.audioToText(media, userName);
        return response;
    }
}
