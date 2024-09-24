package Team_REAP.appserver.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashUtils {

    public static String generateFileHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // 스트리밍 방식으로 파일을 읽기 위해 FileInputStream 사용
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[8192]; // 8KB씩 읽기
            int bytesCount;
            // 파일을 청크 단위로 읽으며 해시 계산
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        // 해시 값을 16진수 문자열로 변환
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // 두 파일의 해시 값을 비교하는 메서드
    public static boolean compareFiles(File file1, File file2) throws IOException, NoSuchAlgorithmException {
        String hash1 = generateFileHash(file1);
        String hash2 = generateFileHash(file2);
        return hash1.equals(hash2);
    }
}
