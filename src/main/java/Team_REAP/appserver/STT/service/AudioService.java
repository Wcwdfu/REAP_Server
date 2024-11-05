package Team_REAP.appserver.STT.service;

import Team_REAP.appserver.DB.mongo.Entity.Script;
import Team_REAP.appserver.DB.mongo.service.ScriptService;
import Team_REAP.appserver.Deprecated.HashUtils;
import Team_REAP.appserver.RAG.RAG.service.ChromaDBService;
import Team_REAP.appserver.STT.dto.AudioUploadDTO;
import Team_REAP.appserver.STT.exception.InvalidFileFormatException;
import Team_REAP.appserver.STT.util.MetadataUtils;
import Team_REAP.appserver.STT.util.STTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioService {


    private final STTUtils sttUtils;
    private final ScriptService scriptService; // 변경된 필드명
    private final ChromaDBService chromaDBService; // 추가된 필드
    private final MetadataUtils metadataUtils;
    private final S3Service s3Service;

    public AudioUploadDTO processAudio(MultipartFile media, String userName, String topic) throws IOException {

        // 올바른 파일 확장자 체크
        validateFileExtension(media);

        // 임시 파일 생성
        File tempFile = null;
        try {
            tempFile = metadataUtils.saveMultipleFileToTmpFile(media);
            // STT 처리
            String sttResult = sttUtils.requestStt(tempFile);

            // 메타데이터 처리
            LocalDateTime creationDateTime = metadataUtils.readCreationTimeFromMp4(tempFile);
            LocalDateTime creationDateTimeKST = metadataUtils.convertToKST(creationDateTime);

            // 날짜 부분 추출
            String creationDateKST = metadataUtils.formatDateTime(creationDateTime, "yyyy-MM-dd");

            // 스크립트 생성
            String scriptContent = sttUtils.makeScript(sttResult, creationDateTimeKST);

            // MongoDB 저장
            String recordId = null;
            try {
                recordId = HashUtils.generateFileHash(tempFile);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String fileName = media.getOriginalFilename();
            String uploadedDate = LocalDate.now().toString();
            String uploadedTime = LocalDateTime.now().toString();

            // Script 객체 생성
            Script script = Script.builder()
                    .recordId(recordId)
                    .userId(userName)
                    .recordName(fileName)
                    .recordedDate(creationDateKST)
                    .uploadedDate(uploadedDate)
                    .uploadedTime(uploadedTime)
                    .topic(topic)
                    .text(scriptContent)
                    .build();

            // MongoDB에 Script 저장
            scriptService.saveScript(script);

            // ChromaDB에 Script 추가
            chromaDBService.addScriptToVectorStore(script);

            // S3 업로드
            String audioS3Url = s3Service.upload(tempFile, fileName, userName, creationDateKST);

            return new AudioUploadDTO(fileName, audioS3Url, recordId);
        } finally {
            // 임시 파일 삭제
//            if (tempFile.exists() && !tempFile.delete()) {
//                System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
//            }
            // 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.info("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private void validateFileExtension(MultipartFile media) {
        List<String> allowedExtensions = Arrays.asList("wav", "m4a");
        String mediaFileName = Objects.requireNonNull(media.getOriginalFilename());
        String fileExtension = mediaFileName.substring(mediaFileName.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedExtensions.contains(fileExtension)) {
            throw new InvalidFileFormatException("Invalid file type - please upload audio file (.m4a, .wav)");
        }
    }
}
