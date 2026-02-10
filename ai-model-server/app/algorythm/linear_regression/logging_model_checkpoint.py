import tensorflow as tf

class LoggingModelCheckpoint(tf.keras.callbacks.ModelCheckpoint):
    def __init__(self, *args, logger=None, model_id=None, **kwargs):
        super().__init__(*args, **kwargs)
        self.logger = logger
        self.model_id = model_id

    def on_epoch_end(self, epoch, logs=None):
        prev_best = getattr(self, 'best', None)
        super().on_epoch_end(epoch, logs)
        if self.save_best_only:
            current_best = getattr(self, 'best', None)
            if prev_best is not None and current_best is not None and current_best < prev_best:
                self.logger.info(f"[Model ID. {self.model_id}] 체크포인트 저장됨 (Epoch {epoch+1}, val_loss={logs.get('val_loss', 'N/A')})")
        else:
            self.logger.info(f"[Model ID. {self.model_id}] 체크포인트 저장됨 (Epoch {epoch+1}, val_loss={logs.get('val_loss', 'N/A')})")
