import os, joblib 
import pandas as pd
from functools import reduce
import numpy as np
import tensorflow as tf
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import train_test_split
from utils.config_provider import get_train_data_root_directory
from utils.db_handler import DBHandler

import matplotlib.pyplot as plt  # 그래프를 그리기 위해 matplotlib 추가
import decimal
import itertools
import threading
from collections import OrderedDict
import logging
from typing import TypedDict, List

from .custom_keras_logger import CustomKerasLogger
from .logging_model_checkpoint import LoggingModelCheckpoint

class InferenceItem(TypedDict):
    time: str
    value: float
    original_value: float

class InferenceResult(TypedDict):
    parameterId: int
    items: List[InferenceItem]

class LinearRegression():
    def __init__(self):
        self.logger = logging.getLogger()

        self.__train_data_root_directory = get_train_data_root_directory()
        self.logger.info(f'self.__train_data_root_directory: {self.__train_data_root_directory}')
        
        self.__model_file_name = 'model.h5'
        self.__x_scaler_file_name = 'scaler_x.pkl'

        self.__model_cache = OrderedDict()  # LRU 캐시를 위한 OrderedDict
        self.__lock = threading.Lock()  # 동시성 제어를 위한 Lock
        # Python은 GIL(Global Interpreter Lock)로 인해 기본적으로 싱글스레드로 동작하지만,
        # TensorFlow와 같은 라이브러리는 내부적으로 멀티스레드를 사용할 수 있습니다.
        # 따라서 멀티스레드 환경에서 데이터 무결성을 보장하기 위해 Lock을 사용합니다.
        self.__cache_limit = 10  # 캐시 크기 제한

    def __calculate_model_memory(self, model):
        """모델 파라미터가 차지하는 메모리를 계산합니다."""
        # 총 파라미터 수
        total_params = model.count_params()

        # 데이터 타입 확인 (기본적으로 float32 사용)
        dtype_size = tf.keras.backend.floatx()  # 'float32' 또는 'float64'
        bytes_per_param = 4 if dtype_size == 'float32' else 8  # float32는 4바이트, float64는 8바이트

        # 메모리 사용량 계산
        memory_usage = total_params * bytes_per_param  # 바이트 단위
        memory_usage_mb = memory_usage / (1024 ** 2)  # MB 단위로 변환

        self.logger.info(f"총 파라미터 수: {total_params}")
        self.logger.info(f"파라미터 데이터 타입: {dtype_size}")
        self.logger.info(f"모델이 차지하는 메모리: {memory_usage_mb:.2f} MB")

        return memory_usage_mb

    def __get_or_load_model(self, model_path):
        """모델을 캐싱하거나 로드하여 반환합니다."""
        with self.__lock:  # 멀티스레드 환경에서 안전성을 보장하기 위해 Lock 사용
            if model_path in self.__model_cache:
                # 캐시에 있는 경우, 사용된 항목을 가장 최근으로 이동
                self.__model_cache.move_to_end(model_path)
                return self.__model_cache[model_path]

            # 캐시에 없는 경우, 모델 로드
            self.logger.info(f"모델 로드 중: {model_path}")
            model = tf.keras.models.load_model(model_path)

            # 모델 메모리 사용량 계산
            self.__calculate_model_memory(model)

            self.__model_cache[model_path] = model

            # 캐시 크기 초과 시, 가장 오래된 항목 제거
            if len(self.__model_cache) > self.__cache_limit:
                removed_model_path, _ = self.__model_cache.popitem(last=False)
                self.logger.info(f"캐시 제거: {removed_model_path}")

            return model

    def __plot_training_history(self, history, file_name):
        """
        학습 과정에서의 손실 및 검증 손실을 시각화하는 함수
        """
        plt.figure(figsize=(10, 6))
        plt.plot(history.history['loss'], label='Train Loss')
        plt.plot(history.history['val_loss'], label='Validation Loss')
        plt.xlabel('Epochs')
        plt.ylabel('Loss')
        plt.title('Training and Validation Loss')
        plt.legend()
        plt.grid(True)

        # 파일을 저장할 경로가 없다면, 경로를 생성한다
        directory = os.path.dirname(file_name)
        if not os.path.exists(directory):
            os.makedirs(directory)

        plt.savefig(file_name)

    def __learning(self, features, X_train, y_train, train_work_directory, model_id=None):
        """
        주어진 입력 데이터를 사용하여 선형 회귀 모델을 학습합니다.
        매개변수:
            features (list): 입력 데이터의 특징(컬럼) 목록.
            X_train (numpy.ndarray): 학습 데이터의 입력 값.
            y_train (numpy.ndarray): 학습 데이터의 타겟 값.
            train_work_directory (str): 학습 결과를 저장할 작업 디렉토리 경로.
        반환값:
            model (tf.keras.Model): 학습된 Keras 모델.
        동작:
            1. 신경망 모델을 정의하고 컴파일합니다.
            2. EarlyStopping 및 ModelCheckpoint 콜백을 설정합니다.
            3. 모델을 학습하고 학습 과정을 시각화합니다.
            4. 가장 성능이 좋은 체크포인트 모델을 로드하여 반환합니다.
        """
        model = tf.keras.Sequential([
            tf.keras.layers.Input(shape=(len(features),)),
            tf.keras.layers.Dense(128, kernel_initializer='he_normal'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.LeakyReLU(alpha=0.1),
            
            tf.keras.layers.Dense(64, kernel_initializer='he_normal'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.LeakyReLU(alpha=0.1),
            
            tf.keras.layers.Dense(32, kernel_initializer='he_normal'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.LeakyReLU(alpha=0.1),
            
            tf.keras.layers.Dropout(0.2),  # 과적합 방지를 위한 Dropout
            tf.keras.layers.Dense(1)  # 출력 레이어 (활성화 함수 없음)
        ])

        # 모델 컴파일 - 학습률 조정
        optimizer = tf.keras.optimizers.Adam(learning_rate=0.0001)
        model.compile(optimizer=optimizer, loss='mse', metrics=['mae'])

        early_stopping = tf.keras.callbacks.EarlyStopping(
            monitor='val_loss', patience=10
        )

        # 체크포인트 설정
        checkpoint_path = train_work_directory + os.path.sep + "model_checkpoint.h5"
        checkpoint = LoggingModelCheckpoint(
            filepath=checkpoint_path,
            monitor='val_loss',
            save_best_only=True,
            verbose=0,
            logger=self.logger,
            model_id=model_id
        )

        epochs = 500
        custom_logger = CustomKerasLogger(self.logger, total_epochs=epochs, model_id=model_id)

        self.logger.info(f"X_train shape: {X_train.shape}")
        self.logger.info(f"y_train shape: {y_train.shape}")

        # 모델 학습
        history = model.fit(
            X_train, y_train, 
            epochs=epochs, 
            batch_size=32, 
            validation_split=0.2, 
            verbose=0,
            callbacks=[early_stopping, checkpoint, custom_logger]
        )

        # 학습 상황 시각화
        self.__plot_training_history(history, train_work_directory + os.path.sep + 'training_history.png')

        # 체크포인트된 모델 로드
        model = tf.keras.models.load_model(checkpoint_path)
        return model
    
    def __train_work_directory(self, site, model_id):
        path_parts = [self.__train_data_root_directory, site, model_id]
        train_work_directory = os.path.join(*path_parts)
        return train_work_directory

    def __fetch_training_dataset(self, site, model_id):
        """
        학습 데이터셋을 가져오는 함수.
        Args:
            site (str): 데이터베이스 핸들러를 초기화하기 위한 사이트 정보.
            model_id (int): AI 모델의 ID로, 관련 파라미터 및 기간 정보를 조회하는 데 사용.
        Returns:
            pd.DataFrame: X 파라미터 데이터와 y 값이 병합된 최종 학습 데이터셋.
        동작:
            1. 데이터베이스에서 모델 ID에 해당하는 X 파라미터와 y 파라미터 데이터를 조회.
            2. X 파라미터 데이터를 DataFrame으로 변환하고 trace_at 기준으로 병합.
            3. y 파라미터 데이터를 DataFrame으로 변환하고 trace_at 기준으로 병합.
            4. 빈 데이터가 있는 경우 전방 채움(ffill) 및 후방 채움(bfill)으로 처리.
            5. 최종적으로 X 파라미터와 y 값을 하나의 DataFrame으로 병합하여 반환.
        """
        db_handler = DBHandler(site)
        ai_model_info = db_handler.select_model_by_id(model_id)

        self.logger.info(f'조회 기간: {ai_model_info.from_date}, {ai_model_info.to_date}')
        y_rows = None
        rows = []

        for x_id in ai_model_info.get_x_parameter_ids():
            parameter_data_list = db_handler.select_parameter_data(x_id, ai_model_info.from_date, ai_model_info.to_date)
            self.logger.info(f'{str(x_id)} size: {str(len(parameter_data_list))}')
            if len(parameter_data_list) == 0:
                failure_reason = f'x_parameter_id {x_id}의 데이터가 존재하지 않아 학습 데이터를 생성하지 못함'
                db_handler.update_model_build_fail_status(model_id, failure_reason)
                raise RuntimeError(failure_reason)
            rows.append(parameter_data_list)

        y_rows = db_handler.select_parameter_data(ai_model_info.y_parameter_id, ai_model_info.from_date, ai_model_info.to_date)
        if len(y_rows) == 0:
            failure_reason = f'y_parameter_id {x_id}의 데이터가 존재하지 않아 학습 데이터를 생성하지 못함'
            db_handler.update_model_build_fail_status(model_id, failure_reason)
            raise RuntimeError(failure_reason)

        # 각 rows의 리스트를 DataFrame으로 변환 (각 데이터객체는 trace_at과 get_value()를 가진다고 가정)
        df_list = []
        for i, data_list in enumerate(rows):
            parameter_id = ai_model_info.get_x_parameter_ids()[i]
            df_temp = pd.DataFrame({
                "trace_at": [item.trace_at for item in data_list],
                f"param_{parameter_id}": [item.get_value() for item in data_list]
            })
            self.logger.info('temp trace_at type: %s', df_temp['trace_at'].dtype)
            df_list.append(df_temp)

        # 여러 DataFrame을 trace_at을 기준으로 outer join하여 하나의 DataFrame으로 병합
        if df_list:
            merged_df = reduce(lambda left, right: pd.merge(left, right, on="trace_at", how="outer"), df_list)
        else:
            merged_df = pd.DataFrame()

        # y_rows도 동일하게 DataFrame으로 변환
        y_df = pd.DataFrame({
            "trace_at": [item.trace_at for item in y_rows],
            "y_value": [item.get_value() for item in y_rows]
        })

        # y_df의 trace_at을 기준으로 병합하기 위해 인덱스 설정
        merged_df.set_index("trace_at", inplace=True)
        y_df.set_index("trace_at", inplace=True)

        # y_df의 trace_at 인덱스에 맞춰 reindex 진행 (미리 존재하지 않는 값은 NaN 처리)
        merged_df = merged_df.reindex(y_df.index)

        # 전방 채움과 후방 채움으로 빈 데이터를 채움
        merged_df = merged_df.ffill().bfill()

        # 빈 데이터가 남아있는지 확인
        if merged_df.isnull().values.any():
            self.logger.info("빈 데이터 존재")
        else:
            self.logger.info("모든 빈 데이터 채움 완료")

        # 최종적으로 y_df의 y_value와 다른 파라미터 데이터를 하나의 DataFrame으로 병합
        final_df = pd.concat([merged_df, y_df], axis=1)
        final_df = final_df.applymap(lambda x: float(x) if isinstance(x, (int, float, decimal.Decimal)) else x)
        self.logger.info(final_df)
        return final_df

    def __fetch_inference_dataset(self, site, model_id, from_date, to_date):
        db_handler = DBHandler(site)
        ai_model_info = db_handler.select_model_by_id(model_id)

        self.logger.info(f'조회 기간: {from_date}, {to_date}')
        rows = []

        # X 파라미터 데이터 조회
        for x_id in ai_model_info.get_x_parameter_ids():
            parameter_data_list = db_handler.select_parameter_data(x_id, from_date, to_date)
            self.logger.info(f'{str(x_id)} size: {str(len(parameter_data_list))}')
            if len(parameter_data_list) == 0:
                raise RuntimeError(f'x_parameter_id {x_id}의 데이터가 존재하지 않아 추론 데이터를 생성하지 못함')
            rows.append(parameter_data_list)

        # y 파라미터 데이터 조회
        y_rows = db_handler.select_parameter_data(ai_model_info.y_parameter_id, from_date, to_date)
        if len(y_rows) == 0:
            raise RuntimeError(f'y_parameter_id {ai_model_info.y_parameter_id}의 데이터가 존재하지 않아 추론 데이터를 생성하지 못함')

        # X 파라미터 데이터를 DataFrame으로 변환
        df_list = []
        for i, data_list in enumerate(rows):
            parameter_id = ai_model_info.get_x_parameter_ids()[i]
            df_temp = pd.DataFrame({
                "trace_at": [item.trace_at for item in data_list],
                f"param_{parameter_id}": [item.get_value() for item in data_list]
            })
            df_list.append(df_temp)

        # y 파라미터 데이터를 DataFrame으로 변환
        y_df = pd.DataFrame({
            "trace_at": [item.trace_at for item in y_rows],
            "y_value": [item.get_value() for item in y_rows]
        })

        # 여러 DataFrame을 trace_at을 기준으로 outer join하여 하나의 DataFrame으로 병합
        merged_df = reduce(lambda left, right: pd.merge(left, right, on="trace_at", how="outer"), df_list)

        # y_df의 trace_at을 기준으로 병합하기 위해 인덱스 설정
        merged_df.set_index("trace_at", inplace=True)
        y_df.set_index("trace_at", inplace=True)

        # y_df의 trace_at 인덱스에 맞춰 reindex 진행 (미리 존재하지 않는 값은 NaN 처리)
        merged_df = merged_df.reindex(y_df.index)

        # 전방 채움으로 빈 데이터를 채움
        merged_df = merged_df.ffill()

        # 최종적으로 y_df의 y_value와 다른 파라미터 데이터를 하나의 DataFrame으로 병합
        final_df = pd.merge(merged_df, y_df, on="trace_at", how="inner")

        # # 빈 데이터가 있는 행 삭제
        final_df.dropna(inplace=True)
        self.logger.info('\n%s',final_df)
        return final_df

    def build(self, site: str, model_id: str) -> None:
        """
        모델을 학습하고 저장하는 함수.
        Args:
            site (str): 학습 데이터와 관련된 사이트 정보.
            model_id (str): 모델 식별자.
        주요 작업:
            1. GPU 가속 사용 여부 확인 및 TensorFlow 버전 출력.
            2. 학습 데이터셋 로드 및 특성과 타겟 분리.
            3. 입력 데이터 스케일링 및 스케일러 저장.
            4. 학습 및 테스트 데이터셋 분리.
            5. 모델 학습 및 저장.
            6. 테스트 데이터셋을 사용한 모델 평가 및 예측 결과 출력.
        """
        # GPU 가속 확인
        self.logger.info("GPU Available: %s", len(tf.config.list_physical_devices('GPU')) > 0)
        self.logger.info("TensorFlow version: %s", tf.__version__)

        with tf.device('/GPU:0'):  # GPU 사용 명시
            train_work_directory = self.__train_work_directory(site, model_id)

            model_file_name = train_work_directory + os.path.sep + self.__model_file_name
            x_scaler_file_name = train_work_directory + os.path.sep + self.__x_scaler_file_name

            # 데이터 로드
            df = self.__fetch_training_dataset(site, model_id)

            # 특성과 타겟 분리 (timestamp 제외)
            features = df.columns[:-1]  # 마지막 컬럼 제외
            target = df.columns[-1]      # 마지막 컬럼이 y

            x = df[features].values
            y = df[target].values

            self.logger.info('features: %s', features)
            self.logger.info('target: %s', target)

            # 데이터 스케일링
            scaler_x = MinMaxScaler()
            x_scaled = scaler_x.fit_transform(x)
            y_scaled = y.reshape(-1, 1)  # y는 스케일링하지 않음

            # 파일을 저장할 경로가 존재하지 않는다면, 경로를 생성함
            if not os.path.exists(train_work_directory):
                os.makedirs(train_work_directory)

            # 스케일러 저장
            joblib.dump(scaler_x, x_scaler_file_name)

            # 훈련 및 테스트 세트 분리
            x_train, x_test, y_train, y_test = train_test_split(x_scaled, y_scaled, test_size=0.3, random_state=777, shuffle=True)

            try:
                model = self.__learning(features, x_train, y_train, train_work_directory, model_id=model_id)
            except Exception as e:
                failure_reason = '학습 진행중 에러가 발생'
                DBHandler(site).update_model_build_fail_status(model_id)
                raise RuntimeError(failure_reason) from e

            # 모델 평가
            loss, mae = model.evaluate(x_test, y_test)
            self.logger.info(f"Test Loss: {loss}")
            self.logger.info(f"Test MAE: {mae}")

            # 예측
            y_pred = model.predict(x_test)

            # 예측 결과 출력
            self.logger.info("예측 결과 (처음 5개):")
            self.logger.info(y_pred[:5])
            self.logger.info("실제 값 (처음 5개):")
            self.logger.info(y_test[:5])

            directory = os.path.dirname(x_scaler_file_name)
            if not os.path.exists(directory):
                os.makedirs(directory)

            # 최종 모델 저장
            model.save(model_file_name)

        DBHandler(site).update_model_complete_status(model_id)

    def inference_range(self, site: str, model_id: str, from_date, to_date) -> InferenceResult:
        train_work_directory = self.__train_work_directory(site, model_id)
        x_scaler_file_name = train_work_directory + os.path.sep + self.__x_scaler_file_name
        model_file_name = train_work_directory + os.path.sep + self.__model_file_name

        scaler_X = joblib.load(x_scaler_file_name)
        self.logger.info(f"Scaler expects {scaler_X.n_features_in_} features")

        df = self.__fetch_inference_dataset(site, model_id, from_date, to_date)

        self.logger.info('columns: %s', df.columns)

        data_times = df.index.tolist()
        input_datas = df[df.columns[0:-1]].values
        y_values = df[df.columns[-1]].values

        for time, input_data, y_value in zip(data_times[:10], input_datas[:10], y_values[:10]):
            self.logger.info('time: %s, input_data: %s, y_value: %s', time, input_data, y_value)

        inferenceItems = []

        # 명시적으로 CPU를 사용
        with tf.device('/CPU:0'):
            model = self.__get_or_load_model(model_file_name)  # 캐싱된 모델 사용

            # 입력 데이터 스케일링
            input_scaled = scaler_X.transform(input_datas)

            # 예측
            y_pred_scaled = model.predict(input_scaled)

            for time, y_pred, y_original in zip(data_times, y_pred_scaled.flatten().tolist(), y_values):
                inferenceItems.append(InferenceItem(
                    time=pd.Timestamp(time).isoformat(),
                    value=y_pred,
                    original_value=y_original
                ))

        db_handler = DBHandler(site)
        ai_model_info = db_handler.select_model_by_id(model_id)

        return InferenceResult(
            parameterId=ai_model_info.y_parameter_id,
            items=inferenceItems
        )
        
    def inference(self, site: str, model_id: str, feature_values) -> float:
        train_work_directory = self.__train_work_directory(site, model_id)
        x_scaler_file_name = train_work_directory + os.path.sep + self.__x_scaler_file_name
        model_file_name = train_work_directory + os.path.sep + self.__model_file_name

        scaler_X = joblib.load(x_scaler_file_name)
        input_scaled = scaler_X.transform([feature_values])

        with tf.device('/CPU:0'):
            model = self.__get_or_load_model(model_file_name)
            y_pred_scaled = model.predict(input_scaled)
            return float(y_pred_scaled.flatten()[0])

    def __generate_step_values(self, min_value, max_value, step):
        step_minus_one = max(step - 1, 1)
        interval = (max_value - min_value) / step_minus_one

        values = [min_value + interval * i for i in range(step)]
        return values

    def simulate_optimal_values(self, site: str, optimization_id: str) -> None:
        db_handler = DBHandler(site)

        optimization_info = db_handler.select_optimization_info(optimization_id)

        x_parameter_ids = []
        step_values = []
        for item in optimization_info.optimization_parameters:
            x_parameter_ids.append(item.parameter_id)
            step_values.append(self.__generate_step_values(item.min_scale_x, item.max_scale_x, item.step))

        # 경우의 수
        combinations = list(itertools.product(*step_values))
        combination_size = len(combinations)

        db_handler.update_optimization_start_status(optimization_id, combination_size)

        train_work_directory = self.__train_work_directory(site, str(optimization_info.ai_model_id))
        x_scaler_file_name = train_work_directory + os.path.sep + self.__x_scaler_file_name
        model_file_name = train_work_directory + os.path.sep + self.__model_file_name

        scaler_X = joblib.load(x_scaler_file_name)
        self.logger.info(f"Scaler expects {scaler_X.n_features_in_} features")

        simulation_results = []  # 결과를 저장할 리스트
        
        # 명시적으로 CPU를 사용
        with tf.device('/CPU:0'):
            model = self.__get_or_load_model(model_file_name)  # 캐싱된 모델 사용

            complete_count = 0

            batch_size = 1000  # 한 번에 처리할 조합의 개수

            last_update_at = None
            progress_update_interval_second = 5
            
            # 최적값
            optimal_value = None
            target_value = optimization_info.target_value

            for i in range(0, len(combinations), batch_size):
                batch_combinations = combinations[i:i + batch_size]  # 1000개 단위로 분리
                input_scaled = scaler_X.transform(batch_combinations)  # 입력 데이터 스케일링
                predict_result = model.predict(input_scaled)  # 예측
                predict_result = predict_result.flatten().tolist()
                
                # 최적값과 가장 가까운 예측값을 찾음
                closest_prediction = min(predict_result, key=lambda x: abs(x - target_value))
                if optimal_value is None or abs(closest_prediction - target_value) < abs(optimal_value - target_value):
                    optimal_value = closest_prediction

                # combination 값과, 예측값을 하나의 dict으로 병합
                input_with_predict_data = [
                    {
                        # { id1: v1, id2: v2 }와 같은 형태로 dict을 생성 후 js의 스프레드 연산처럼 y값을 포함하는 객체로 다시 생성함
                        **{ x_parameter_ids[idx]: combination[idx] for idx in range(len(x_parameter_ids)) },
                        "y": prediction
                    }
                    for combination, prediction in zip(batch_combinations, predict_result)
                ]

                simulation_results.extend(input_with_predict_data)  # 결과를 리스트에 추가
                
                # 디버깅용
                # for item in input_with_predict_data:
                #     self.logger.info(item)

                complete_count += len(predict_result)

                current_time = pd.Timestamp.now()
                # 마지막 업데이트 시간이 없거나 n초 이상 경과했을 경우 업데이트
                if last_update_at is None or (current_time - last_update_at).total_seconds() > progress_update_interval_second:
                    self.logger.info(f"[id: {optimization_id}] 시뮬레이션 진행률: {complete_count}/{combination_size} ({(complete_count / combination_size) * 100:.2f}%)")
                    db_handler.update_optimization_complete_count(optimization_id, complete_count)
                    last_update_at = current_time
            
            db_handler.update_optimization_success(optimization_id, complete_count, optimal_value, simulation_results)

if __name__ == "__main__":

    site = 'local'
    model_id = '46'

    machine = LinearRegression()

    machine.build(site, model_id)
    exit()

    # 주어진 데이터
    data = [
        ["2025-03-25 14:01:34", 1008.0, -155.996, -10.009, -10.009],
        ["2025-03-25 14:01:39", 1008.0, -158.769, -12.782, -12.782],
        ["2025-03-25 14:09:56", 0.0, 0.0, 197.664, 197.664],
        ["2025-03-25 14:12:36", 1008.0, -151.547, -5.56, -5.56],
        ["2025-03-25 14:14:19", 0.0, 0.0, 197.664, 197.664],
        ["2025-03-25 14:14:40", 1813.0, -196.272, 1.1, 1.1],
        ["2025-03-25 14:14:45", 1813.0, -200.028, -2.656, -2.656]
    ]

    # 데이터 변환
    data = np.array(data)  # 리스트를 numpy 배열로 변환
    features = data[:, 1:-1]  # 첫 번째 컬럼(시간) 제외, 마지막 컬럼(타겟값) 제외

    result = machine.inference(site, model_id, features)
    self.logger.info('예측 결과: %s', result)