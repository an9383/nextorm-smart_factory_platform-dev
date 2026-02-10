# Proxy Status Gateway

서버 상태 모니터링을 위한 WebFlux 기반 리액티브 게이트웨이 서비스입니다.
`건창스치로폴` 업체의 요구사항에 맞는 기능 제공을 위해 처음 개발되었습니다.

## 개요

Proxy Status Gateway는 여러 서버의 상태를 실시간으로 모니터링하고, REST API를 통해 연결 상태 정보를 제공하는 Spring Boot WebFlux 애플리케이션입니다.

## 주요 기능

- 실시간 서버 상태 모니터링 (ping 기반)
- WebFlux 기반 리액티브 REST API 제공
- 설정 가능한 모니터링 간격 및 타임아웃
- 상태 변경 시 자동 로깅

## 빠른 시작

### 1. 프로젝트 빌드

```bash
./gradlew build
```

### 2. 서버 설정

`src/main/resources/servers.json` 파일에 모니터링할 서버 정보를 설정합니다:

```json
[
  {
    "name": "웹서버",
    "ip": "192.168.0.30"
  },
  {
    "name": "데이터베이스서버",
    "ip": "192.168.0.31"
  }
]
```

### 3. 애플리케이션 실행

```bash
java -jar build/libs/proxy-status-gateway-0.0.1-SNAPSHOT.jar
```

### 4. 상태 확인

서버가 시작되면 http://localhost:10001 에서 API를 사용할 수 있습니다.

```bash
# 전체 서버 상태 조회
curl http://localhost:10001/api/status

# 특정 서버 상태 조회  
curl http://localhost:10001/api/status/웹서버
```

## 설정

### 환경변수

다음 환경변수로 애플리케이션을 설정할 수 있습니다:

| 환경변수                                  | 설명           | 기본값                    |
|---------------------------------------|--------------|------------------------|
| `PROXY_STATUS_GW_PORT`                | 서버 포트        | 10001                  |
| `PROXY_STATUS_GW_CONFIG_PATH`         | 서버 설정 파일 경로  | classpath:servers.json |
| `PROXY_STATUS_GW_PING_TIMEOUT_MS`     | 핑 타임아웃 (ms)  | 1000                   |
| `PROXY_STATUS_GW_PING_INTERVAL_MS`    | 핑 간격 (ms)    | 1000                   |
| `PROXY_STATUS_GW_PING_MAX_CONCURRENT` | 최대 동시 핑 처리 수 | 30                     |
| `PROXY_STATUS_GW_LOG_LEVEL`           | 로그 레벨        | INFO                   |

## 서버 설정 파일 (servers.json)

모니터링할 서버들은 JSON 배열 형태로 설정합니다:

```json
[
  {
    "name": "서버이름",
    "ip": "IP주소"
  }
]
```

**주의사항:**

- `name`은 고유해야 하며, API 경로에서 사용됩니다
- `ip`는 유효한 IPv4 주소여야 합니다
- 파일은 UTF-8 인코딩으로 저장해야 합니다

### 로그 확인

애플리케이션은 다음과 같은 로그를 출력합니다:

- 서버 상태 변경 시: `INFO` 레벨
- API 요청 처리: `DEBUG` 레벨
- 상세한 핑 처리: `TRACE` 레벨

## 개발

### 프로젝트 구조

```
src/
├── main/
│   ├── java/com/nextorm/extensions/proxystatusgateway/
│   │   ├── controller/          # REST API 컨트롤러
│   │   ├── service/             # 비즈니스 로직
│   │   ├── repository/          # 데이터 접근 계층
│   │   ├── dto/                 # 데이터 전송 객체
│   │   └── config/              # 설정 클래스
│   └── resources/
│       ├── application.yml      # 애플리케이션 설정
│       └── servers.json         # 서버 목록 설정
└── test/                        # 테스트 코드
```

## API 문서

상세한 API 사용법은 [API_Documentation.md](./API_Documentation.md) 파일을 참조하세요.

## 트러블슈팅

### 일반적인 문제

1. **서버가 시작되지 않는 경우**
    - 포트가 이미 사용 중인지 확인
    - `PROXY_STATUS_GW_PORT` 환경변수로 다른 포트 사용

2. **서버 상태가 UNKNOWN으로 표시되는 경우**
    - `servers.json` 파일의 IP 주소가 정확한지 확인
    - 네트워크 연결 및 방화벽 설정 확인

3. **메모리 사용량이 높은 경우**
    - `PROXY_STATUS_GW_PING_MAX_CONCURRENT` 값을 낮춰보세요
    - JVM 힙 메모리 설정: `-Xmx512m`