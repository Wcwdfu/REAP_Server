package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.MongoUserService;
import Team_REAP.appserver.STT.dto.AudioFullDataDto;
import Team_REAP.appserver.STT.dto.AudioMetadataDTO;
import Team_REAP.appserver.STT.service.S3Service;
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
@RequestMapping("/audio")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @Autowired
    private MongoUserService mongoUserService;

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

    @Operation(summary = "음성 데이터 및 정보 삭제 - 지금은 S3만 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAudio(@RequestParam String userName,
                                              @RequestParam String date,
                                              @RequestParam String fileName){

        s3Service.deleteFile(userName, date, fileName);

        return ResponseEntity.ok("success audio delete");
    }


    @Operation(summary = "음성 파일의 메타 데이터 제공 - 최근", description = "클라이언트로부터 유저 식별 정보를 받으면 해당 유저가 최근에 변환한 음성에 대한 메타데이터를 가져옵니다.")
    @GetMapping("/api/detail/{userid}/record-script")
    public ResponseEntity<Object> showRecentRecordList(@PathVariable("userid") String userid){

        log.info("userid = {}", userid);

        List<Script> recentScripts = mongoUserService.findRecentScriptsByUserId(userid);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script recentScript : recentScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(recentScript.getRecordName(),recentScript.getRecordedDate() ,recentScript.getUploadedDate(), recentScript.getUploadedTime(), recentScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }

    @Operation(summary = "음성 파일의 메타 데이터 제공 - 특정 날짜", description = "클라이언트가 유저 식별 정보 및 원하는 날짜 정보를 주면 해당 날짜에 해당하는 메타데이터들을 가져옵니다.")
    @GetMapping("/api/detail/{userid}/{recordedDate}/record-script") // 임시로 mongoDb에서 Record를 가져오도록 만들었다.
    public ResponseEntity<Object> showAudioScript(@PathVariable("userid") String userid,
                                                  @PathVariable("recordedDate") String recordedDate){

        // TODO : userid 등등의 뭔가를 가져와서 mongodb 객체 id를 찾을 수 있도록 해야함

        List<Script> simpleScripts = mongoUserService.findScriptsByUserIdAndRecordedDate(userid, recordedDate);
        List<AudioMetadataDTO> audioMetadataDTOS = new ArrayList<>();

        for (Script simpleScript : simpleScripts) {
            AudioMetadataDTO audioMetadataDTO = new AudioMetadataDTO(simpleScript.getRecordName(),simpleScript.getRecordedDate() ,simpleScript.getUploadedDate(), simpleScript.getUploadedTime(), simpleScript.getTopic());
            audioMetadataDTOS.add(audioMetadataDTO);
        }


        return ResponseEntity.status(HttpStatus.OK).body(audioMetadataDTOS);
    }

    @Operation(summary = "음성 파일의 모든 제공 - 특정 날짜", description = "클라이언트가 유저 식별 정보 및 원하는 날짜 정보를 주면 해당 날짜에 해당하는 메타데이터들과 스크립트 정보들을 가져옵니다.")
    @GetMapping("/api/detail/{userid}/{recordedDate}/total-script") // 임시로 mongoDb에서 Record를 가져오도록 만들었다.
    public ResponseEntity<Object> showAudioScript(@PathVariable("userid") String userid,
                                                  @PathVariable("recordedDate") String recordedDate,
                                                  @RequestParam("recordName") String recordName){

        // TODO : userid 등등의 뭔가를 가져와서 mongodb 객체 id를 찾을 수 있도록 해야함

        Script script = mongoUserService.findScriptByUserIdAndRecordedDateAndRecordName(userid, recordedDate, recordName);

        AudioFullDataDto audioFullDataDto = new AudioFullDataDto(script.getUserId(), script.getRecordName(),
                                                                script.getRecordedDate(), script.getUploadedDate(), script.getUploadedTime(),
                                                                script.getTopic(), script.getText());

        return ResponseEntity.status(HttpStatus.OK).body(audioFullDataDto);
    }
}
