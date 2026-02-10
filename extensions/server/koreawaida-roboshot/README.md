# 한국와이다 Roboshot 인터페이스 모듈

## 개요

한국와이다(KoreaWaida) 화낙Roboshot 장비와의 인터페이스를 제공하는 Spring Boot 기반 확장 모듈입니다.
서버가 실행중인 환경에 있는 실행 파일을 통해 장비의 실시간 모니터링 데이터를 수집하고 REST API로 제공합니다.

## 주요 기능

- 장비별 모니터링 데이터 실시간 수집
- REST API를 통한 데이터 조회 서비스
- 외부 실행 파일과의 프로세스 통신

## API 명세

### 모니터링 데이터 조회

현재 장비의 모니터링 데이터를 조회합니다.

**Endpoint**: `POST /api/monitor/data`

**Request Body**:

```json
{
  "machineId": 1,
  "exePath": "C:\\path\\to\\monitoring\\executable.exe"
}
```

**Response**: 데이터는 쉼표로 구분된 문자열 형태로 반환되며, 데이터 순서별 의미는 문서 마지막에서 다룹니다

```json
[
  "데이터1",
  "데이터2",
  "데이터3"
]
```

**Parameters**:

- `machineId` (int): 모니터링할 장비의 고유 ID
- `exePath` (string): 모니터링 데이터 수집용 실행 파일의 전체 경로

## 프로젝트 구조

```
src/main/java/com/nextorm/extensions/koreawaida/roboshot/
├── RoboshotInterfaceApplication.java    # Spring Boot 메인 애플리케이션
├── MonitorDataController.java           # REST API 컨트롤러
├── MonitorDataService.java              # 모니터링 데이터 수집 서비스
└── MonitorDataRequestDto.java           # 요청 데이터 전송 객체
```

## 동작 원리

1. REST API를 통해 장비 ID와 실행 파일 경로를 전달받습니다
2. ProcessBuilder를 사용하여 외부 실행 파일을 호출합니다
3. 실행 파일의 출력 결과를 MS949 인코딩으로 읽어옵니다
4. 쉼표로 구분된 데이터를 파싱하여 리스트로 반환합니다
5. 에러 발생 시 빈 리스트를 반환합니다

## 에러 처리

- 외부 프로세스 실행 실패 시 에러 메시지 로깅 후 빈 리스트 반환
- "ERROR:" 접두사가 포함된 결과는 에러로 처리
- 프로세스 타임아웃 및 예외 상황에 대한 안전한 처리

## 주의사항

- 실행 파일의 경로가 정확해야 합니다

## 배포 방식

- 로컬에서 빌드 후 한국와이다 원격PC에 업로드 후 실행

## 반환 데이터 순서별 의미

- 관련 문서가 따로 존재하지않아, SFP 시스템에 세팅되어있던 값을 기반으로 정리하였습니다
- 반환되는 쉼표로 구분된 데이터의 순서별 의미는 다음과 같습니다

| 순서  | 의미                         |
|-----|----------------------------|
| 1   | 날짜                         |
| 2   | 시각                         |
| 3   | 기계_상태                      |
| 4   | 사이클_시간                     |
| 5   | 사출시간                       |
| 6   | 계량시간                       |
| 7   | 최소쿠션                       |
| 8   | 계량위치                       |
| 9   | Peak_사출압                   |
| 10  | SHOT수                      |
| 11  | 양품수                        |
| 12  | 불량품수                       |
| 13  | 사이클_남음                     |
| 15  | V-P_전환_위치                  |
| 18  | 노즐1_온도                     |
| 20  | 바렐1_온도                     |
| 21  | 바렐2_온도                     |
| 22  | 바렐3_온도                     |
| 25  | Hopper설정온도                 |
| 26  | 계량_시작_위치                   |
| 27  | 금형파일ID                     |
| 28  | 로트수                        |
| 29  | 컨테이너(불량_수)                 |
| 30  | 계량토크1                      |
| 31  | 계량토크2                      |
| 32  | 압력_모니터_1                   |
| 33  | 계량토크3                      |
| 34  | 계량토크4                      |
| 35  | 압력_모니터_2                   |
| 36  | 계량토크5                      |
| 37  | 계량토크                       |
| 38  | 압력_모니터_3                   |
| 41  | 압력_모니터_4                   |
| 42  | 압력_모니터_5                   |
| 43  | 소비전력량                      |
| 44  | 양부판별                       |
| 45  | Peak_사출압_시간                |
| 46  | Peak_사출압_위치                |
| 57  | Eject_편차토크                 |
| 58  | 총_Shot수                    |
| 59  | 형폐시간                       |
| 60  | V-P_전환_압력                  |
| 64  | 사출_시작_압력                   |
| 65  | 압축_량                       |
| 66  | 압축_압력                      |
| 67  | V-P_보정량                    |
| 68  | 유량_피크                      |
| 69  | 역류_량                       |
| 109 | 형체시간                       |
| 110 | 취출시간                       |
| 111 | 체류_시간                      |
| 112 | Eject_편차토크(ave)            |
| 138 | Hot_runner1_온도             |
| 139 | Hot_runner2_온도             |
| 140 | Hot_runner3_온도             |
| 141 | Hot_runner4_온도             |
| 142 | Hot_runner5_온도             |
| 143 | Hot_runner6_온도             |
| 144 | Hot_runner7_온도             |
| 145 | Hot_runner8_온도             |
| 146 | Hot_runner9_온도             |
| 147 | Hot_runner10_온도            |
| 148 | Hot_runner11_온도            |
| 149 | Hot_runner12_온도            |
| 150 | Hot_runner13_온도            |
| 151 | Hot_runner14_온도            |
| 152 | Hot_runner15_온도            |
| 153 | Hot_runner16_온도            |
| 154 | Hot_runner17_온도            |
| 155 | Hot_runner18_온도            |
| 156 | Hot_runner19_온도            |
| 157 | Hot_runner20_온도            |
| 158 | Hot_runner21_온도            |
| 159 | Hot_runner22_온도            |
| 160 | Hot_runner23_온도            |
| 161 | Hot_runner24_온도            |
| 163 | 사출_개시_위치                   |
| 165 | Consump_Servo              |
| 166 | Consump_Servo_Regeneration |
| 167 | Consump_Heater             |
| 168 | Consump_Others             |
| 182 | 형개시간                       |
| 183 | 꺼내기시간                      |
