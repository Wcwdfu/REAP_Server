package Team_REAP.appserver.DB.S3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

@Slf4j
@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String DIR_NAME = "user";

    public String upload(String fileName, MultipartFile multipartFile, String extend) throws IOException { // dirName의 디렉토리가 S3 Bucket 내부에 생성됨

        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(fileName, uploadFile, extend);
    }

    private String upload(String fileName,File uploadFile,String extend) {
        String newFileName = DIR_NAME + "/" + fileName+extend;
        String uploadImageUrl = putS3(uploadFile, newFileName);

        removeNewFile(uploadFile);  // convert()함수로 인해서 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    public String upload(File uploadFile, String fileName, String userName, String audioDate) {

        String newFileName = userName + "/" + audioDate  + "/" + fileName;
        String uploadImageUrl = putS3(uploadFile, newFileName);

        removeNewFile(uploadFile);  // convert()함수로 인해서 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile));
                        //.withCannedAcl(CannedAccessControlList.PublicRead)	// PublicRead 권한으로 업로드 됨
        //);
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        }else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        log.info(file.getOriginalFilename());
        File convertFile = new File(file.getOriginalFilename()); // 업로드한 파일의 이름
        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    public ResponseEntity<byte[]> download(String fileName) throws IOException {
        S3Object awsS3Object = amazonS3.getObject(new GetObjectRequest(bucket, DIR_NAME + "/" + fileName));
        S3ObjectInputStream s3is = awsS3Object.getObjectContent();
        byte[] bytes = s3is.readAllBytes();

        String downloadedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("audio/wav"));
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", downloadedFileName);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    public void deleteFile(String userId, String date, String fileName){
        try {
            String path = userId + "/" + date + "/" + fileName;
            // S3 클라이언트를 사용하여 파일 삭제
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, path));
            log.info("S3Service - 파일이 성공적으로 삭제되었습니다: {}", fileName);
        } catch (Exception e) {
            log.info("S3Service - 파일 삭제 중 오류가 발생했습니다: {}", e.getMessage());
        }
    }

    public void moveFile(String userId, String recordedDate, String oldRecordName, String newRecordName) {
        String oldPath = userId + "/" + recordedDate + "/" + oldRecordName;
        String newPath = userId + "/" + recordedDate + "/" + newRecordName; // 확장자 모름

        log.info("S3Service - oldPath: {}", oldPath);

        // 파일 복사 후 삭제
        amazonS3.copyObject(bucket, oldPath, bucket, newPath);
        amazonS3.deleteObject(bucket, oldPath);
    }

}
