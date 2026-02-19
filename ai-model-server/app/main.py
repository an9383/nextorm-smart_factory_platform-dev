#! /usr/bin/python
import logging
import sys
import io
import os
from logging.handlers import RotatingFileHandler
from application import create_app

def init_logger():
    # sys.stdout을 UTF-8로 재설정
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

    # StreamHandler: stdout에 로그 출력
    stream_handler = logging.StreamHandler(sys.stdout)
    stream_formatter = logging.Formatter('%(levelname)s:%(filename)s:%(funcName)s:%(lineno)d:%(message)s')
    stream_handler.setFormatter(stream_formatter)

    # 1. Define your log directory path
    log_dir = 'logs'

    # 2. Create the directory if it doesn't exist
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)

    # RotatingFileHandler: 파일 로그(10MB, 최대 5개, UTF-8)
    # file_handler = RotatingFileHandler('logs/app.log', maxBytes=10 * 1024 * 1024, backupCount=5, encoding='utf-8')
    file_handler = RotatingFileHandler(f'{log_dir}/app.log', maxBytes=10 * 1024 * 1024, backupCount=5, encoding='utf-8')
    file_formatter = logging.Formatter('%(asctime)s [PID:%(process)d %(processName)s] %(levelname)s:%(filename)s:%(funcName)s:%(lineno)d - %(message)s')
    file_handler.setFormatter(file_formatter)

    logger = logging.getLogger()
    logger.handlers = []  # 기존 핸들러 제거
    logger.addHandler(stream_handler)
    logger.addHandler(file_handler)
    logger.setLevel(logging.INFO)


init_logger()
application = create_app()

if __name__ == '__main__':
    application.run(host = '0.0.0.0', debug = True)