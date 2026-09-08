import sys
sys.path.insert(0, '.')
from app.ml.training import TrainingPipeline
pipeline = TrainingPipeline('artifacts/')
pipeline.train_baseline()
