# 158 서버 기준으로 작성된 스크립트,
# 158 서버와 환경이 다르다면 스크립트를 적절히 수정하여 사용
docker build -f Dockerfile_model_server_base --tag model-server-base:cudnn-12.1.0 .