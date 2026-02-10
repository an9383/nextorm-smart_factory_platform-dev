import logging  # 로깅 모듈 추가
from flask import Flask, request, jsonify
from algorythm.linear_regression.linear_regression import LinearRegression
from utils.db_handler import DBHandler

def create_app():
    app = Flask(__name__)

    # logger 인스턴스 생성
    logger = logging.getLogger()

    # LinearRegression 인스턴스를 애플리케이션 수준에서 생성하여 재사용
    linear_regression_instance = LinearRegression()

    def parse_request_data(required_fields):
        """요청 데이터를 파싱하고 필수 필드를 검증하는 유틸리티 함수"""
        try:
            body = request.get_json()
            for field in required_fields:
                if field not in body:
                    raise ValueError(f"필수 필드가 누락되었습니다: {field}")
                if body[field] is None:
                    raise ValueError(f"필수 필드의 값이 누락되었습니다: {field}")
            return body, None, None
        except ValueError as e:
            logger.error(f"요청 파싱 오류: {e}")
            return None, {'success': False, 'message': str(e)}, 400
        except Exception as e:
            logger.error(f"예상치 못한 오류: {e}")
            return None, {'success': False, 'message': '요청 형식이 잘못되었습니다'}, 400

    @app.before_request
    def log_request_info():
        logger.info(
            f"요청: {request.method} {request.url} "
            f"remote_addr={request.remote_addr} "
            f"body={request.get_data(as_text=True)}"
        )

    @app.route('/api/linear-regression/build', methods=['POST'])
    def build_linear_model():
        required_fields = ['site', 'modelId']
        body, error_response, status_code = parse_request_data(required_fields)
        if body is None:
            return jsonify(error_response), status_code

        site = body['site']
        model_id = body['modelId']

        try:
            DBHandler(site)
        except Exception as e:
            logger.error(f"DBHandler 오류: {e}", exc_info=True)
            return jsonify({'success': False, 'message': '데이터베이스 설정에 접근하지 못했습니다'}), 500

        try:
            linear_regression_instance.build(site, str(model_id))
            return jsonify({'success': True, 'message': '모델 생성에 성공했습니다'}), 200
        except Exception as e:
            logger.error(f"모델 생성 오류: {e}", exc_info=True)
            return jsonify({'success': False, 'message': '모델 생성에 실패했습니다'}), 500

    @app.route('/api/linear-regression/inference-range', methods=['POST'])
    def inference_linear_model():
        required_fields = ['site', 'modelId', 'fromDate', 'toDate']
        body, error_response, status_code = parse_request_data(required_fields)
        if body is None:
            return jsonify(error_response), status_code

        site = body['site']
        model_id = body['modelId']
        from_date = body['fromDate']
        to_date = body['toDate']

        try:
            result = linear_regression_instance.inference_range(site, str(model_id), from_date, to_date)
            return jsonify({'success': True, 'message': '추론이 완료되었습니다', 'data': result}), 200
        except Exception as e:
            logger.error(f"추론 오류: {e}", exc_info=True)
            return jsonify({'success': False, 'message': f'추론에 실패했습니다 ({e})'}), 500

    @app.route('/api/linear-regression/inference', methods=['POST'])
    def inference_single():
        required_fields = ['site', 'modelId', 'featureValues']
        body, error_response, status_code = parse_request_data(required_fields)
        if body is None:
            return jsonify(error_response), status_code

        site = body['site']
        model_id = body['modelId']
        feature_values = body['featureValues']

        try:
            result = linear_regression_instance.inference(site, str(model_id), feature_values)
            return jsonify({'success': True, 'message': '단일 예측이 완료되었습니다', 'data': result}), 200
        except Exception as e:
            logger.error(f"단일 예측 오류: {e}")
            return jsonify({'success': False, 'message': f'단일 예측에 실패했습니다 ({e})'}), 500

    @app.route('/api/linear-regression/simulate-optimal-values', methods=['POST'])
    def simulate_optimal_values():
        required_fields = ['site', 'optimizationId']
        body, error_response, status_code = parse_request_data(required_fields)
        if body is None:
            return jsonify(error_response), status_code

        site = body['site']
        optimization_id = body['optimizationId']

        try:
            linear_regression_instance.simulate_optimal_values(site, optimization_id)
            return jsonify({'success': True, 'message': '시뮬레이션이 완료되었습니다'}), 200
        except Exception as e:
            logger.error(f"시뮬레이션 오류: {e}", exc_info=True)
            return jsonify({'success': False, 'message': f'최적 값 시뮬레이션에 실패했습니다 ({e})'}), 500

    return app