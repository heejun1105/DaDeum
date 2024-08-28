# 다듬 STT 프로젝트

다듬은 VITO STT 오픈 API를 활용하여 정확하고 효율적인 음성 인식 서비스를 제공하는 음성-텍스트 변환(STT) 웹 애플리케이션입니다. 이 프로젝트는 다른 애플리케이션과의 쉬운 통합을 위한 RESTful API를 구현합니다.

## 주요 기능

- VITO STT 오픈 API를 사용한 음성-텍스트 변환
- 사용자 인증 및 관리
- 쉬운 통합을 위한 RESTful API
- 사용량 추적
- 파일 업로드 및 변환을 위한 웹 인터페이스

## 기술 스택

- Java 17
- Spring Boot 3.3.2
- Gradle
- Oracle 데이터베이스
- 서버 사이드 렌더링을 위한 Thymeleaf
- 프론트엔드 스타일링을 위한 Bootstrap

## 사전 요구 사항

- JDK 17
- Gradle
- Oracle 데이터베이스
- VITO STT API 접근 정보

## 설정 방법

1. 저장소 클론:
   ```
   git clone https://github.com/yourusername/dadeum-stt.git
   cd dadeum-stt
   ```

2. 데이터베이스 구성:
   - Oracle 데이터베이스 생성
   - `application.properties`에 데이터베이스 연결 정보 업데이트

3. VITO API 설정:
   - `application.properties`에 VITO 클라이언트 ID와 비밀키 추가:
     ```
     vito.client_id=your_client_id
     vito.client_secret=your_client_secret
     ```

4. 프로젝트 빌드:
   ```
   ./gradlew build
   ```

5. 애플리케이션 실행:
   ```
   ./gradlew bootRun
   ```

이제 애플리케이션이 `http://localhost:8080`에서 실행되어야 합니다.

## API 사용법

### 인증

API를 사용하려면 API 키가 필요합니다. 웹 인터페이스를 통해 계정을 등록하고 프로필 페이지에서 API 키를 생성하세요.

### 오디오 변환

엔드포인트: `POST /dadeum/openapi/file`

헤더:
- `X-API-KEY`: 귀하의 API 키
- `Content-Type`: multipart/form-data

본문:
- `file`: 변환할 오디오 파일 (지원 형식: WAV, MP3 등)

cURL을 사용한 예시:
```bash
curl -X POST -H "X-API-KEY: your_api_key" -F "file=@path/to/your/audio.wav" http://localhost:8080/dadeum/openapi/file
```

응답:
```json
{
  "transcription": "변환된 텍스트가 여기에 표시됩니다",
  "fileName": "audio.wav",
  "duration": 10.5,
  "fileSize": 1048576,
  "processingTime": 2.3
}
```

## 웹 인터페이스

웹 인터페이스는 다음 기능을 제공합니다:
- 사용자 등록 및 로그인
- 변환을 위한 오디오 파일 업로드
- 변환 내역 조회
- 사용 통계

브라우저에서 `http://localhost:8080`으로 이동하여 웹 인터페이스에 접근하세요.


## 감사의 말

- 다듬에 관심가져주셔서 감사합니다.


