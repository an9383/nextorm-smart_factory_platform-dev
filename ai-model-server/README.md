# Smart Factory Platform - AI Model Server

## 프로젝트 개요

AI Model Server는 스마트 팩토리 플랫폼의 일부로, 머신러닝 모델을 학습, 추론 및 최적화 시뮬레이션하는 기능을 제공합니다. 이 서버는 Flask를 기반으로 하며, WSGI를 통해 Apache와 통합되어
동작합니다.

## 주요 기능

- **모델 학습**: 주어진 데이터셋을 기반으로 선형 회귀 모델을 학습합니다.
- **모델 추론**: 학습된 모델을 사용하여 새로운 데이터에 대한 예측을 수행합니다.
- **최적화 시뮬레이션**: 최적의 입력 값을 찾기 위한 시뮬레이션을 실행합니다.

## 디렉토리 구조

```
.
├── app
│   ├── application.py          # Flask 애플리케이션 생성
│   ├── config.ini              # 설정 파일
│   ├── main.py                 # 애플리케이션 진입점
│   ├── algorythm
│   │   └── linear_regression
│   │       └── LinearRegression.py  # 선형 회귀 알고리즘 구현
│   └── utils
│       ├── ConfigProvider.py   # 설정 제공 유틸리티
│       └── DBHandler.py        # 데이터베이스 핸들러
├── image-build
│   ├── apache-flask.conf       # Apache 설정 파일
│   ├── build-base-image.sh     # Docker 베이스 이미지 빌드 스크립트
│   ├── Dockerfile              # Docker 이미지 빌드 파일
│   ├── Dockerfile_model_server_base # 모델 서버 베이스 이미지 빌드 파일
│   └── requirements.txt        # Python 의존성 목록
├── logs
│   └── ...                     # 로그파일 저장소
├── train_data
│   └── ...                     # 학습 데이터 및 모델 파일 저장소
├── docker-compose.yml          # Docker Compose 설정 파일
└── README.md                   # 프로젝트 설명 파일
```

## 설치 및 실행

### 주의사항

- 실행 환경에 맞는 CUDA 버전을 선택하여 사용해야 합니다. 현재 설정은 서버 환경에서의 동작을 기준으로 작성되었습니다. 로컬 환경에서 실행 시, 베이스 이미지의 CUDA 버전과 로컬의 CUDA 버전을 확인 후
  사용하세요.
- 정상적인 실행을 위해서는 `config.ini` 파일에 `site, DB` 정보가 제대로 입력되어야 합니다

### 1. Docker 환경 설정

1. os(윈도우의 경우 wsl)에 `nvidia-container-toolkit`을
   설치합니다. [설치 가이드](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html)를 참조하세요.
    - NVIDIA 컨테이너 툴킷은 사용자가 GPU 가속 컨테이너를 빌드하고 실행할 수 있도록 하는 라이브러리와 유틸리티 모음입니다


2. `image-build` 디렉토리에서 베이스 이미지를 빌드합니다:
   ```sh
   cd image-build
   ./build-base-image.sh   
   ```

3. `docker-compose.yml` 파일을 사용하여 컨테이너를 실행합니다:
   ```sh
   docker compose up -d
   ```
    - Dockerfile을 수정한 경우에는 아래 명령어를 먼저 사용
   ```sh
   docker compose build
   ```

### 2. Flask 애플리케이션 실행

`app/main.py`를 실행하여 Flask 애플리케이션을 로컬에서 실행할 수 있습니다:

```sh
python app/main.py
```

## API 엔드포인트

### 1. 모델 학습 (GPU 사용)

- **URL**: `/api/linear-regression/build`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "site": "local",
      "modelId": "46"
  }
  ```
- **Response 예시**:
  ```json
  {
    "success": true,
    "message": "모델 생성에 성공했습니다"
  }
  ```

### 2. 구간 모델 추론 (CPU 사용)

- **URL**: `/api/linear-regression/inference-range`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "site": "local",
      "modelId": "46",
      "fromDate": "2025-01-01T00:00:00",
      "toDate": "2025-01-31T23:59:59"
  }
  ```
- **Response 예시**:
  ```json
  {
    "success": true,
    "message": "추론이 완료되었습니다",
    "data": {
      "parameterId": 123,
      "items": [
        {
          "time": "2025-01-01T00:00:00",
          "value": 123.45,
          "original_value": 120.00
        }
        // ...
      ]
    }
  }
  ```

### 3. 단일 입력값 모델 추론 (CPU 사용)

- **URL**: `/api/linear-regression/inference`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "site": "local",
      "modelId": "46",
      "featureValues": [1.23, 4.56, 7.89]
  }
  ```
- **Response 예시**:
  ```json
  {
    "success": true,
    "message": "단일 예측이 완료되었습니다",
    "data": 123.45
  }
  ```

### 4. 최적화 시뮬레이션 (CPU 사용)

- **URL**: `/api/linear-regression/simulate-optimal-values`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
      "site": "local",
      "optimizationId": "123"
  }
  ```
- **Response 예시**:
  ```json
  {
    "success": true,
    "message": "시뮬레이션이 완료되었습니다"
  }
  ```