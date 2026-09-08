import sys
sys.path.insert(0, '.')
import numpy as np
from app.ml.training import TrainingPipeline
pipeline = TrainingPipeline('artifacts/')
X, y_ts, y_cb, y_ff = pipeline.create_prototype_dataset()
print('y_ts sum (positives):', np.sum(y_ts))
print('y_cb sum (positives):', np.sum(y_cb))
print('y_ff sum (positives):', np.sum(y_ff))
