package Team_REAP.appserver.STT.controller;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.MongoUserService;
import Team_REAP.appserver.STT.dto.RecentScriptDTO;
import Team_REAP.appserver.STT.service.S3Service;
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

    //s3에 잘 들어가나 테스트해볼려고 만든것
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

    @GetMapping(path = "/download/{fileName}")
    public ResponseEntity<byte[]> getPetImage(
            @PathVariable String fileName
    ) throws IOException {
        return s3Service.download(fileName);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAudio(@RequestParam String userName,
                                              @RequestParam String date,
                                              @RequestParam String fileName){

        s3Service.deleteFile(userName, date, fileName);

        return ResponseEntity.ok("success audio delete");
    }

    @GetMapping("/api/detail/{userid}/record-script")
    public ResponseEntity<Object> showRecentRecordList(@PathVariable("userid") String userid){

        log.info("userid = {}", userid);

        List<Script> recentScripts = mongoUserService.findRecentScriptsByUserId(userid);
        List<RecentScriptDTO> recentScriptDTOS = new ArrayList<>();

        for (Script recentScript : recentScripts) {
            RecentScriptDTO recentScriptDTO = new RecentScriptDTO(recentScript.getRecordName(), recentScript.getUploadedDate(), recentScript.getUploadedTime());
            recentScriptDTOS.add(recentScriptDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(recentScriptDTOS);
    }
}
