## i18n 정의되지 않은 키 확인 방법
<hr/>

#### vue 및 js 파일에서 사용하고 있으나 메시지가 정의되지 않은 키를 확인하는 방법은 다음과 같습니다

1. 프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다
    ```
    npm run check-missing-i18n
    ```
2. 정의되지 않은 키 목록이 `./i18n/missingKeys.output` 파일에 저장됩니다

<br/>

#### missing 키 확인 절차는 다음과 같습니다
1. portal에서 정의된 언어 정보를 조회합니다
2. 언어 정보별로 정의된 메시지 목록을 조회합니다
3. (1, 2) 정보를 이용하여 i18n에서 사용하는 언어파일(ex. ko.json, en.json)을 생성합니다
4. vue-i18n-extract를 실행하여 missing 키 report를 생성합니다
5. 생성된 report를 확인하여 missing 키 정보를 ./i18n/missingKeys.output 파일에 저장합니다

* 위 작업은 `./i18nExtract.js` 파일을 통해 수행됩니다
