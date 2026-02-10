import time
import tensorflow as tf

class CustomKerasLogger(tf.keras.callbacks.Callback):
    def __init__(self, logger, total_epochs=None, model_id=None):
        super().__init__()
        self.logger = logger
        self.total_epochs = total_epochs
        self.model_id = model_id
        self.epoch_start_time = None

    def on_epoch_begin(self, epoch, logs=None):
        self.epoch_start_time = time.time()

    def on_epoch_end(self, epoch, logs=None):
        logs = logs or {}
        total_epochs = self.total_epochs if self.total_epochs is not None else '?'
        model_id_str = f"[Model ID. {self.model_id}] " if self.model_id is not None else ""
        elapsed = time.time() - self.epoch_start_time if self.epoch_start_time else 0
        log_str = f"{model_id_str}Epoch {epoch+1}/{total_epochs}: elapsed={elapsed:.2f}s, "
        log_str += ", ".join([f"{k}={v:.4f}" for k, v in logs.items()])
        self.logger.info(log_str)

    def on_train_begin(self, logs=None):
        self.train_start_time = time.time()

    def on_train_end(self, logs=None):
        elapsed = time.time() - self.train_start_time if hasattr(self, 'train_start_time') else 0
        model_id_str = f"[Model ID. {self.model_id}] " if self.model_id is not None else ""
        self.logger.info(f"{model_id_str}학습 완료, 총 소요 시간: {elapsed:.2f}s")
