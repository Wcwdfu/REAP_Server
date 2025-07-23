<div align="center">
  <img width="150" height="150" alt="Logo-removebg-preview" src="https://github.com/user-attachments/assets/71bf9b4c-7a3a-4a5e-b547-91c4ac8bd52e" />
</div>

# REAP_Service (Server)
24년도 컴퓨터공학부 졸업프로젝트 REAP(Record Everything And Play) Service 입니다. 
<br>
발표영상 : [유튜브 링크](https://www.youtube.com/watch?v=q-nz9tahR1w)
<br>


<img width="900" height="1300" alt="image" src="https://github.com/user-attachments/assets/9c4592c4-ba66-4895-abd1-9474e9b511be" />

녹음 파일을 넣어주면 텍스트로 변환(STT)하여 대화스크립트로 저장합니다.<br>
해당 내용들을 기반으로 텍스트나 음성으로 질의응답을 할 수 있습니다.(RAG, TTS)<br>
즉 녹음 파일 데이터에 기반하여 음성 질의응답 chat GPT와 같은 서비스 입니다.

## 소프트웨어 아키텍처
![image](https://github.com/user-attachments/assets/9a549bbf-e677-4c45-9f09-e2b39a8359cc)

## 데이터 저장 구조

| 항목              | 저장소          |
|-------------------|-----------------|
| 회원정보 관리      | MySQL          |
| JWT 토큰 저장     | Redis          |
| 대화 스크립트 저장 | MongoDB        |
| 음성 파일 저장     | AWS S3         |
| 임베딩 데이터 저장 | ChromaDB       |
