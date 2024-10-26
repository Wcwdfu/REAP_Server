//package Team_REAP.appserver.Deprecated;
//
//import Team_REAP.appserver.DB.mongo.service.MongoUserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@Service
//public class ReapService {
//    private final MongoUserService mongoUserService;
//    @Value("${gpt.model}")
//    private String model;
//
//    @Value("${gpt.api.url}")
//    private String apiUrl;
//    private final RestTemplate restTemplate;
//
//    public String questionAndAnswering(String name, String date, String prompt) {
//        String condition = "받은 날짜를 0000-00-00 형태로 만들어 줘, 근데 0000은 2024로 바꿔줘";
//
//
//        // 사용자가 질문한 것을 원하는 데이터로 변환
//        GPTRequest pre_request = new GPTRequest(
//                model,condition, date, 1,256,1,2,2);
//
//        GPTResponse pre_gptResponse = restTemplate.postForObject(
//                apiUrl
//                , pre_request
//                , GPTResponse.class
//        );
//
//        String refinedDate = pre_gptResponse.getChoices().get(0).getMessage().getContent();
//        System.out.println(refinedDate);
//
//        // 유저 데이터 찾아서 gpt에 넣기
//        List<User> userDatas = mongoUserService.readByNameAndDate(name, refinedDate);
//        StringBuilder timelog = new StringBuilder();
//        for (User data : userDatas) {
//            timelog.append(data.getDate()).append(" ");
//            timelog.append(data.getTime()).append(" ");
//            timelog.append(data.getText()).append("\n");
//        }
//        String dialog = new String(timelog);
//
//        System.out.println(dialog);
//
//        GPTRequest request = new GPTRequest(
//                model,prompt, dialog , 1,256,1,2,2);
//
//        GPTResponse gptResponse = restTemplate.postForObject(
//                apiUrl
//                , request
//                , GPTResponse.class
//        );
//
//        return gptResponse.getChoices().get(0).getMessage().getContent();
//    }
//}
