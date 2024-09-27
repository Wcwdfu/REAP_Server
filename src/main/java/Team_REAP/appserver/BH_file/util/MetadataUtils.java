package Team_REAP.appserver.BH_file.util;

import lombok.extern.slf4j.Slf4j;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class MetadataUtils {

    // 임시 파일 생성 및 저장 메서드
    public File saveMultipleFileToTmpFile(MultipartFile media) throws IOException {

        // MultipartFile을 임시 파일로 저장
        log.info("MultipartFile을 임시 파일로 저장");
        File tempFile = Files.createTempFile("upload", media.getOriginalFilename()).toFile();
        media.transferTo(tempFile);
        return tempFile;

    }

    //MP4 파일에서 생성 시간 추출 메서드
    public LocalDateTime readCreationTimeFromMp4(File tempFile) throws IOException {
        log.info("MP4 파일 메타데이터 읽기");
        // MP4 파일 메타데이터 읽기
        try(IsoFile isoFile = new IsoFile(tempFile.getAbsolutePath())) {
            log.info(tempFile.getAbsolutePath());
            MovieHeaderBox mvhd = isoFile.getBoxes(MovieHeaderBox.class, true).get(0);
            long creationTime = mvhd.getCreationTime().getTime();

            return LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneOffset.UTC);
        }
    }

    // UTC 시간을 KST로 변환하는 메서드
    public LocalDateTime convertToKST(LocalDateTime creationDateTime){
        log.info("시간대 변환");
        // 시간대를 변환 (예: 한국 시간대로 변환)
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        return creationDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
    }

    // LocalDateTime을 지정된 패턴으로 포맷팅하는 메서드
    public String formatDateTime(LocalDateTime dateTime, String pattern){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
