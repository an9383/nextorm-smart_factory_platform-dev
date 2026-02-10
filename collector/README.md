### Collector 모듈 기능

    1. 데이터 수집을 담당하는 컬렉터의 타입을 정의        

    2. DCP를 참고하여 데이터 소스로부터 데이터를 수집하고, 1차 가공 후 프로세싱을 위해 카프카에 프로듀싱한다
    ( 카프카 정보는 DCP에 포함되어 있음 )

<br/>

### 필수 Args

| 키                     | 설명                                                                       | 예시                    |
|-----------------------|--------------------------------------------------------------------------|-----------------------|
| config-server-address | 컬렉터 실행에 필요한 설정 정보를 제공하는 configServer 서버 주소<br/> (configServer == portal) | http://localhost:8081 |
| config-name           | 설정 정보 이름 ( configServer api 요청에 사용함 )                                    | ToolAB                |

<br/>

### 컬렉터 추가 방법

1. Collector 인터페이스 구현 (`com.nextorm.collector.collector.Collector`)   
   `현재는 반드시 (DataCollectPlan, ObjectMapper)를 받는 생성자로 구현해야 합니다.`
2. CollectorType Enum에 추가 (`com.nextorm.collector.collector.CollectorType`)
3. CollectorType Enum의 `DEFINITION_VERSION` 필드의 값을 증가   
   `DB collector_defines 테이블의 version값 보다 크게 변경`
4. 컬렉터 재기동

<br/>

### 컬렉터 - 포탈간 컬렉터 정보 공유 과정

+ 컬렉터의 종류는 컬렉터 모듈에서 정의하고, 포탈은 컬렉터로부터 정의 정보를 받아 DB에 저장 및 화면에 제공한다
    + 컬렉터 정보 전달 과정
        + `컬렉터` 기동시 포탈의 `[POST] /api/config/collector/type-definition` API로 컬렉터 정보를 전달한다
        + `포탈`은 API로 들어온 컬렉터 정의 버전 정보와 DB의 컬렉터 정의 버전을 비교한다
        + 이 때, API로 들어온 버전이 저장된 버전보다 크면 기존 데이터를 제거하고 새로 들어온 데이터로 갱신한다




