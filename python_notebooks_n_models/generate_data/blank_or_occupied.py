from datetime import datetime
import os, sys
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from keras.preprocessing.image import ImageDataGenerator

image_size = (32, 32)
batch_size = 16

current_time = datetime.now().strftime("%Y-%m-%d_%H_%M_%S")

PATH_TO_DATA = 'blank_or_occupied_data'
MODEL_NAME = f'pieces_model'
MODEL_NAME_TIME = MODEL_NAME + "_" + current_time

datagen_white_fields = ImageDataGenerator(
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest')
print(datagen_white_fields)
datagen_black_fields = ImageDataGenerator(
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest')
print(datagen_black_fields)
train_white_fields = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/train/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True
)
test_white_fields = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/test/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 2,
    shuffle=True
)
train_black_fields = datagen_black_fields.flow_from_directory(
    f'{PATH_TO_DATA}/train/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True
)
test_black_fields = datagen_black_fields.flow_from_directory(
   f'{PATH_TO_DATA}/test/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 2,
    shuffle=True
)

white_model = keras.Sequential(
    [
        keras.Input(shape=(32,32,1)), # 32x32 grayscale
        layers.Conv2D(32, kernel_size=(3, 3), activation="relu"),
        layers.MaxPooling2D(pool_size=(2, 2)),
        layers.Conv2D(64, kernel_size=(3, 3), activation="relu"),
        layers.MaxPooling2D(pool_size=(2, 2)),
        layers.Conv2D(128, kernel_size=(3, 3), activation="relu"),
        layers.MaxPooling2D(pool_size=(2, 2)),
        layers.Flatten(),
        # layers.Dropout(0.2),
        layers.Dense(256, activation="relu"),
        layers.Dense(2, activation="softmax"), # 2 classes
    ]
)

white_model.summary()

black_model = keras.models.clone_model(white_model)

white_model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['categorical_accuracy'])
epochs = 10

history = white_model.fit(
    train_white_fields,
    epochs=epochs,
    validation_data=test_white_fields
    )

white_model.save_weights(f'white_{MODEL_NAME}_all.h5')


black_model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['categorical_accuracy'])
epochs = 10

history = black_model.fit(
    train_black_fields,
    epochs=epochs,
    validation_data=test_black_fields
    )
black_model.save_weights(f'black_{MODEL_NAME}_all.h5')

converter_white_model = tf.lite.TFLiteConverter.from_keras_model(white_model)
converter_white_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_white_model.convert()

f = open(f'white_{MODEL_NAME}_all.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

converter_black_model = tf.lite.TFLiteConverter.from_keras_model(black_model)
converter_black_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_black_model.convert()

f = open(f'black_{MODEL_NAME}_all.tflite', "wb")
f.write(tflite_quantized_model)
f.close()