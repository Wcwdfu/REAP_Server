package Team_REAP.appserver.View.controller;

import Team_REAP.appserver.DB.S3.service.S3Service;
import Team_REAP.appserver.DB.chroma.service.ChromaDBService;
import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.dto.ModifiedScriptDto;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.STT.dto.AudioMetadataDTO;
import Team_REAP.appserver.STT.dto.AudioUploadDTO;
import Team_REAP.appserver.STT.dto.ScriptTextDataDTO;
import Team_REAP.appserver.STT.service.AudioService;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class ViewController {

    private final S3Service s3Service;
    private final ScriptService scriptService;
    private final ChromaDBService chromaDBService;
    private final AudioService audioService;


    // 1. 음성 파일 업로드
    @Operation(summary = "음성 -> S3저장, 스크립트 변환 후 DB 저장", description = "사용자로부터 음성을 받으면 네이버 STT를 통해서 음성을 텍스트로 변환합니다." +
            "STT 결과를 가지고 스크립트로 만들어 음성 파일은 S3에 저장하고 스크립트 관련 내용은 MongoDb에 저장합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Object> recognizeMediaFromURL(
            @RequestParam("media") MultipartFile media,
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

    // 2.메인 뷰에서 최근 업로드한 음성파일들 메타데이터 제공
    @Operation(summary = "음성 파일의 메타 데이터 제공 - 최근", description = "유저가 최근에 변환한 음성파일에 대한 정보를 반환합니다.")
    @GetMapping("/meta/recent")
    public ResponseEntity<Object> showRecentRecordList(@AuthUser String userid){

        log.info("userid = {}", userid);

        List<Script> recentScripts = scriptService.findRecentScriptsByUserId(userid);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script recentScript : recentScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(recentScript.getRecordId(), recentScript.getRecordName(),recentScript.getRecordedDate() ,recentScript.getUploadedDate(), recentScript.getUploadedTime(), recentScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }


    //3.특정 날짜의 메타데이터 제공
    @Operation(summary = "음성 파일의 메타 데이터 제공 - 특정 날짜", description = "해당 날짜에 해당하는 음성파일 목록들을 반환합니다.")
    @GetMapping("/meta/{date}") // 임시로 mongoDb에서 Record를 가져오도록 만들었다.
    public ResponseEntity<Object> showAudioScript(@AuthUser String userid,
                                                  @PathVariable("date") String recordedDate){


        List<Script> simpleScripts = scriptService.findScriptsByUserIdAndRecordedDate(userid, recordedDate);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script simpleScript : simpleScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(simpleScript.getRecordId(), simpleScript.getRecordName(),simpleScript.getRecordedDate() ,simpleScript.getUploadedDate(), simpleScript.getUploadedTime(), simpleScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }


        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }



    // 4.특정 날짜의 대화스크립트 제공

    @Operation(summary = "음성 파일의 대화 내용 제공 - 특정 날짜", description = "특정 날짜에 해당하는 대화 내용을 JSON 형식으로 반환합니다.")
    @GetMapping("/script/{date}")
    public ResponseEntity<List<ScriptTextDataDTO>> showAudioScript(
            @AuthUser String userid,
            @PathVariable("date") String recordedDate,
            @RequestParam("recordId") String recordId) {

        List<ScriptTextDataDTO> audioTextDataDtos = scriptService.getFormattedAudioData(userid, recordedDate, recordId);
        return ResponseEntity.status(HttpStatus.OK).body(audioTextDataDtos);
    }



    //5.파일 수정 - recordName과 topic 동시에 업데이트
    @Operation(summary = "음성 데이터 - 제목, 주제 업데이트",
            description = "클라이언트가 스크립트 제목과 주제를 업데이트 할 수 있습니다.")
    @PutMapping("/script")
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


    // 6. 파일 삭제 - s3,mongo,chroma db에서 데이터 삭제
    @Operation(summary = "음성 데이터 및 정보 삭제",
            description = "클라이언트가 음성 데이터 및 정보에 대한 삭제 요청을 보내면, S3,몽고DB,chromaDB에 있는 데이터를 삭제시킵니다.")
    @DeleteMapping("/script/{date}")
    public ResponseEntity<String> deleteAudio(
            @AuthUser String userId,
            @PathVariable String date,
            @RequestParam String fileName,
            @RequestParam String recordId
    ){
        scriptService.deleteScript(userId, recordId);
        s3Service.deleteFile(userId, date, fileName);
        chromaDBService.deleteDocuments(List.of(recordId));

        return ResponseEntity.status(HttpStatus.OK).body("success audio delete");
    }


}
