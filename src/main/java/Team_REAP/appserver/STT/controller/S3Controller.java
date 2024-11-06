package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.STT.dto.AudioFullDataDto;
import Team_REAP.appserver.STT.dto.AudioMetadataDTO;
import Team_REAP.appserver.STT.dto.ScriptTextDataDTO;
import Team_REAP.appserver.STT.service.S3Service;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/auth")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @Autowired
    private ScriptService scriptService;

    @Operation(summary = "S3 업로드 - 테스트")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAudio(@RequestParam("fileName") String fileName,
                                              @RequestPart("audioFile") MultipartFile multipartFile) throws IOException {

        String extend = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        String url = s3Service.upload(fileName, multipartFile, extend); // filename, multipartFile, extend

        log.info(url);
        return new ResponseEntity<>(url,null, HttpStatus.OK);
        //log.info(file.getName()); // file
        //log.info(file.getOriginalFilename()); // 사용자가 설정한 이름
        //log.info(file.getContentType()); // audio/wave
        //log.info(String.valueOf(file.getResource())); // MultipartFile resource [file]
        //log.info(String.valueOf(file.getSize())); // 2603086

    }

    @Operation(summary = "S3 다운로드 - 테스트")
    @GetMapping(path = "/download/{fileName}")
    public ResponseEntity<byte[]> getPetImage(
            @PathVariable String fileName
    ) throws IOException {
        return s3Service.download(fileName);
    }

    @Operation(summary = "음성 데이터 및 정보 삭제",
            description = "클라이언트가 음성 데이터 및 정보에 대한 삭제 요청을 보내면, S3와 몽고DB에 있는 데이터를 삭제시킵니다.")
    @DeleteMapping("/api/detail/script/{date}/delete")
    public ResponseEntity<String> deleteAudio(@AuthUser String userId,
                                              @PathVariable String date,
                                              @RequestParam String fileName,
                                              @RequestParam String recordId){

        // MongoDB에서 정보 삭제
        scriptService.deleteScript(userId, recordId);
        // S3에서 삭제
        s3Service.deleteFile(userId, date, fileName);

        return ResponseEntity.status(HttpStatus.OK).body("success audio delete");
    }


    @Operation(summary = "음성 파일의 메타 데이터 제공 - 최근", description = "클라이언트로부터 유저 식별 정보를 받으면 해당 유저가 최근에 변환한 음성에 대한 메타데이터를 가져옵니다.")
    @GetMapping("/api/detail/record-script")
    public ResponseEntity<Object> showRecentRecordList(@AuthUser String userid){

        log.info("userid = {}", userid);

        List<Script> recentScripts = scriptService.findRecentScriptsByUserId(userid);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script recentScript : recentScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(recentScript.getRecordName(),recentScript.getRecordedDate() ,recentScript.getUploadedDate(), recentScript.getUploadedTime(), recentScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }

    @Operation(summary = "음성 파일의 메타 데이터 제공 - 특정 날짜", description = "클라이언트가 유저 식별 정보 및 원하는 날짜 정보를 주면 해당 날짜에 해당하는 메타데이터들을 가져옵니다.")
    @GetMapping("/api/detail/{recordedDate}/record-script") // 임시로 mongoDb에서 Record를 가져오도록 만들었다.
    public ResponseEntity<Object> showAudioScript(@AuthUser String userid,
                                                  @PathVariable("recordedDate") String recordedDate){

        // TODO : userid 등등의 뭔가를 가져와서 mongodb 객체 id를 찾을 수 있도록 해야함

        List<Script> simpleScripts = scriptService.findScriptsByUserIdAndRecordedDate(userid, recordedDate);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script simpleScript : simpleScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(simpleScript.getRecordName(),simpleScript.getRecordedDate() ,simpleScript.getUploadedDate(), simpleScript.getUploadedTime(), simpleScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }


        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }

    @Operation(summary = "음성 파일의 대화 내용 제공 - 특정 날짜", description = "특정 날짜에 해당하는 대화 내용을 JSON 형식으로 반환합니다.")
    @GetMapping("/api/detail/{recordedDate}/total-script")
    public ResponseEntity<List<ScriptTextDataDTO>> showAudioScript(
            @AuthUser String userid,
            @PathVariable("recordedDate") String recordedDate,
            @RequestParam("recordName") String recordName) {

        List<ScriptTextDataDTO> audioTextDataDtos = scriptService.getFormattedAudioData(userid, recordedDate, recordName);
        return ResponseEntity.status(HttpStatus.OK).body(audioTextDataDtos);
    }

}
