from datetime import datetime
import os, sys
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from keras.preprocessing.image import ImageDataGenerator
import random

image_size = (32, 32)
batch_size = 64

current_time = datetime.now().strftime("%Y-%m-%d_%H_%M_%S")

PATH_TO_DATA = 'pieces_data'
MODEL_NAME_TIME = f'pieces_model_{current_time}'
MODEL_NAME = f'pieces_model'


def add_noise(img):
    '''Add random noise to an image'''
    VARIABILITY = 50
    deviation = VARIABILITY * random.random()
    noise = np.random.normal(0, deviation, img.shape)
    img += noise
    np.clip(img, 0., 255.)
    return img


datagen_white_fields_white = ImageDataGenerator(
    rotation_range=5,
    horizontal_flip=False,
    fill_mode='nearest')

print(datagen_white_fields_white)
datagen_black_fields_white = ImageDataGenerator(
    rotation_range=5,
    horizontal_flip=False,
    fill_mode='nearest', )

print(datagen_black_fields_white)

datagen_white_fields_black = ImageDataGenerator(
    rotation_range=5,
    horizontal_flip=False,
    fill_mode='nearest')

print(datagen_white_fields_black)
datagen_black_fields_black = ImageDataGenerator(
    rotation_range=5,
    horizontal_flip=False,
    fill_mode='nearest', )

print(datagen_black_fields_black)

train_white_fields_white = datagen_white_fields_white.flow_from_directory(
    f'{PATH_TO_DATA}/train/white_fields/white',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=2,
    shuffle=True
)
test_white_fields_white = datagen_white_fields_white.flow_from_directory(
    f'{PATH_TO_DATA}/test/white_fields/white',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=3,
    shuffle=True
)
train_black_fields_white = datagen_black_fields_white.flow_from_directory(
    f'{PATH_TO_DATA}/train/black_fields/white',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=2,
    shuffle=True
)
test_black_fields_white = datagen_black_fields_white.flow_from_directory(
    f'{PATH_TO_DATA}/test/black_fields/white',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=3,
    shuffle=True
)

train_white_fields_black = datagen_white_fields_black.flow_from_directory(
    f'{PATH_TO_DATA}/train/white_fields/black',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=2,
    shuffle=True
)
test_white_fields_black = datagen_white_fields_black.flow_from_directory(
    f'{PATH_TO_DATA}/test/white_fields/black',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=3,
    shuffle=True
)
train_black_fields_black = datagen_black_fields_black.flow_from_directory(
    f'{PATH_TO_DATA}/train/black_fields/black',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=2,
    shuffle=True
)
test_black_fields_black = datagen_black_fields_black.flow_from_directory(
    f'{PATH_TO_DATA}/test/black_fields/black',
    target_size=image_size,
    class_mode='categorical',
    color_mode='grayscale',
    seed=3,
    shuffle=True
)

model = keras.Sequential(
    [
        keras.Input(shape=(32, 32, 1)),  # 32x32 grayscale
        layers.Conv2D(64, kernel_size=(3, 3), activation="relu"),
        layers.MaxPooling2D(pool_size=(2, 2)),
        layers.Conv2D(64, kernel_size=(3, 3), activation="relu"),
        layers.MaxPooling2D(pool_size=(2, 2)),
        layers.Flatten(),
        layers.Dense(64, activation="relu"),
        layers.Dense(6, activation="softmax"),  # 6 classes
    ]
)

model.summary()

def learn_and_save(model_template, color_field, color_piece, train_data, test_data):
    model = keras.models.clone_model(model_template)
    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
    epochs = 1

    history = model.fit(
        train_data,
        epochs=epochs,
        validation_data=test_data
    )

    model.save_weights(f'archive/pieces/{color_field}_{MODEL_NAME_TIME}_{color_piece}.h5')

    converter_model = tf.lite.TFLiteConverter.from_keras_model(model)
    converter_model.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_quantized_model = converter_model.convert()

    f = open(f'archive/pieces/{color_field}_{MODEL_NAME_TIME}_{color_piece}.tflite', "wb")
    f.write(tflite_quantized_model)
    f.close()

    f = open(f'{color_field}_{MODEL_NAME}_{color_piece}.tflite', "wb")
    f.write(tflite_quantized_model)
    f.close()


learn_and_save(model, 'white', 'white', train_white_fields_white, test_white_fields_white)
learn_and_save(model, 'white', 'black', train_white_fields_black, test_white_fields_black)
learn_and_save(model, 'black', 'white', train_black_fields_white, test_black_fields_white)
learn_and_save(model, 'black', 'black', train_black_fields_black, test_black_fields_black)


