# Proxy Status Gateway API 문서

## 개요

Proxy Status Gateway는 서버 상태 모니터링을 위한 WebFlux 기반 리액티브 REST API를 제공합니다. 구성된 서버들의 상태를 실시간으로 조회할 수 있습니다.

## 기본 정보

- **Base URL**: `http://localhost:10001/api`
- **Content-Type**: `application/json`
- **응답 형태**: JSON (리액티브 스트림)

## API 엔드포인트

### 1. 전체 서버 상태 조회

모든 서버의 상태 정보를 조회합니다.

#### 요청

```
GET /api/status
```

#### 응답

**성공 시 (Status Code: 200 OK)**

```json
[
  {
    "serverName": "IP는 바르게 입력해주세요",
    "status": "UP",
    "lastCheckedTime": "2025-08-06T14:30:00"
  }
]
```

**에러 시 (Status Code: 500 Internal Server Error)**

```json
{
  "status": 500,
  "reason": "서버 상태 조회 중 오류가 발생했습니다"
}
```

#### 사용 예시

```bash
curl -X GET "http://localhost:10001/api/status" -H "Accept: application/json"
```

### 2. 특정 서버 상태 조회

지정된 서버의 상태 정보를 조회합니다.

#### 요청

```
GET /api/status/{serverName}
```

-- **경로 파라미터**: `serverName` - 조회할 서버의 이름 (URL 인코딩 필요)

#### 경로 파라미터

| 파라미터       | 타입     | 설명        | 예시               |
|------------|--------|-----------|------------------|
| serverName | String | 조회할 서버 이름 | `IP는 바르게 입력해주세요` |

#### 응답

**성공 시 (Status Code: 200 OK)**

```json
{
  "serverName": "IP는 바르게 입력해주세요",
  "status": "UP",
  "lastCheckedTime": "2025-08-06T14:30:00"
}
```

**서버를 찾을 수 없는 경우 (Status Code: 404 Not Found)**

```json
{
  "status": 404,
  "reason": "서버 'nonexistent-server'를 찾을 수 없습니다"
}
```

**에러 시 (Status Code: 500 Internal Server Error)**

```json
{
  "status": 500,
  "reason": "서버 상태 조회 중 오류가 발생했습니다"
}
```

#### 사용 예시

-- 서버 이름 자리에는 'IP는 바르게 입력해주세요'과 같이 URL 인코딩을 한 문자열을 사용해야 합니다

```bash
curl -X GET "http://localhost:10001/api/status/IP%EB%8A%94%20%EB%B0%94%EB%A5%B4%EA%B2%8C%20%EC%9E%85%EB%A0%A5%ED%95%B4%EC%A3%BC%EC%84%B8%EC%9A%94" -H "Accept: application/json
```

## 응답 데이터 형식

### 정상 응답 (ServerStatusResponse)

| 필드              | 타입                    | 설명        | 예시                            |
|-----------------|-----------------------|-----------|-------------------------------|
| serverName      | String                | 서버 이름     | `"IP는 바르게 입력해주세요"`            |
| status          | String                | 서버 상태     | `"UP"`, `"DOWN"`, `"UNKNOWN"` |
| lastCheckedTime | String (ISO DateTime) | 마지막 조회 시간 | `"2025-08-06T14:30:00"`       |

### 에러 응답 (ErrorResponse)

| 필드     | 타입     | 설명         | 예시                |
|--------|--------|------------|-------------------|
| status | Number | HTTP 상태 코드 | `404`, `500`      |
| reason | String | 에러 발생 이유   | `"서버를 찾을 수 없습니다"` |

### 서버 상태 값

- **UP**: 서버가 정상적으로 동작 중
- **DOWN**: 서버가 응답하지 않음
- **UNKNOWN**: 상태를 확인할 수 없음

## 에러 상황별 응답

### 400 Bad Request

잘못된 요청 파라미터나 인코딩 문제가 발생한 경우

**발생 상황:**

- 인코딩되지 않은 한국어나 특수문자가 URL에 포함된 경우
- 잘못된 요청 파라미터 형식
- 요청 데이터 파싱 오류

### 404 Not Found

특정 서버 조회 시 해당 서버가 존재하지 않는 경우

**응답 예시:**

```json
{
  "status": 404,
  "reason": "서버 'nonexistent-server'를 찾을 수 없습니다"
}
```

**발생 상황:**

- 존재하지 않는 서버 이름으로 조회 시
- 서버 설정 파일(servers.json)에 등록되지 않은 서버 조회 시

### 500 Internal Server Error

서버 내부 오류 발생 시

**응답 예시:**

```json
{
  "status": 500,
  "reason": "서버 상태 조회 중 오류가 발생했습니다"
}
```

**발생 상황:**

- 서버 설정 파일을 읽을 수 없는 경우
- 네트워크 연결 문제
- 애플리케이션 내부 오류

## 참고사항

- 이 API는 WebFlux 기반의 리액티브 스트림을 사용합니다
- 응답은 실시간으로 스트리밍되며, 서버 상태는 설정된 간격으로 자동 업데이트됩니다
- 서버 이름은 인코딩 해서 요청해야 합니다.
- 400에러는 제외한 모든 에러 응답은 일관된 형식(`{status, reason}`)을 따릅니다
