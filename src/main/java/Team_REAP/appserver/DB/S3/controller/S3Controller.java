package Team_REAP.appserver.DB.S3.controller;

import Team_REAP.appserver.DB.chroma.service.ChromaDBService;
import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.STT.dto.AudioMetadataDTO;
import Team_REAP.appserver.STT.dto.ScriptTextDataDTO;
import Team_REAP.appserver.DB.S3.service.S3Service;
import Team_REAP.appserver.common.login.ano.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class S3Controller {
    private final S3Service s3Service;

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



}
