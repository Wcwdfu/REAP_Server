# REAP_Service (Server)
24년도 컴퓨터공학부 졸업프로젝트 REAP(Record Everything And Play) Service 입니다.
<br>

<img src="https://github.com/user-attachments/assets/70bae643-3baf-4dd6-8de0-c594de86a414" alt="REAP_Service" width="300">


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
