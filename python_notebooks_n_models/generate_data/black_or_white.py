from datetime import datetime
import os, sys
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from keras.preprocessing.image import ImageDataGenerator
import cv2
import matplotlib.pyplot as plt

image_size = (32, 32)
batch_size = 32

current_time = datetime.now().strftime("%Y-%m-%d_%H_%M_%S")

PATH_TO_DATA = 'black_or_white_data'
MODEL_NAME = f'black_or_white_model'
MODEL_NAME_TIME = MODEL_NAME + "_" + current_time


def rgb_to_hsv(image):
    return cv2.cvtColor(image,cv2.COLOR_RGB2HSV)


datagen_white_fields = ImageDataGenerator(
    rescale=1./255,
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest',
        brightness_range=[0.65,1.35],
        width_shift_range=0.15,
        height_shift_range=0.15,
        #preprocessing_function=rgb_to_hsv
)
print(datagen_white_fields)
datagen_black_fields = ImageDataGenerator(
        rescale=1./255,
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest',
        width_shift_range=0.15,
        height_shift_range=0.15,
        brightness_range=[0.65,1.35],
        #preprocessing_function=rgb_to_hsv
)
print(datagen_black_fields)
train_white_fields = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/train/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'rgb',
    seed = 12,
    shuffle=True
)
test_white_fields = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/test/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'rgb',
    seed = 12,
    shuffle=True
)
train_black_fields = datagen_black_fields.flow_from_directory(
    f'{PATH_TO_DATA}/train/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'rgb',
    seed = 12,
    shuffle=True
)
test_black_fields = datagen_black_fields.flow_from_directory(
   f'{PATH_TO_DATA}/test/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'rgb',
    seed = 12,
    shuffle=True
)
# Used to generate graphs, uncomment below lines to generate graphs without learn new models
# plt.figure(figsize=(12, 12))
# for i in range(0, 15):
#     plt.subplot(5, 3, i+1)
#     for X_batch, Y_batch in test_black_fields:
#         image = X_batch[0]
#         color = "Czarny" if Y_batch[0][0] == 1 else "Biały"
#         plt.imshow(image)
#         plt.xlim(0,32)
#         plt.ylim(32,0)
#         plt.title(color, size=14)
#         break
# plt.tight_layout()
# plt.show()
# exit()


white_model = keras.Sequential(
    [
    layers.Conv2D(32, kernel_size=(3, 3), activation="relu", input_shape=(32,32,3)),
    layers.BatchNormalization(),
    layers.MaxPooling2D(pool_size=(2, 2)),
    layers.Dropout(0.25),

    layers.Conv2D(64, kernel_size=(3, 3), activation="relu", kernel_regularizer=keras.regularizers.l1_l2(l1=0.01, l2=0.01)),
    layers.BatchNormalization(),
    layers.MaxPooling2D(pool_size=(2, 2)),
    layers.Dropout(0.25),

    layers.Conv2D(128, kernel_size=(3, 3), activation="relu", kernel_regularizer=keras.regularizers.l1_l2(l1=0.01, l2=0.01)),
    layers.BatchNormalization(),
    layers.MaxPooling2D(pool_size=(2, 2)),
    layers.Dropout(0.25),

    layers.Conv2D(256, kernel_size=(3, 3), activation="relu", padding="SAME"),
    layers.BatchNormalization(),
    layers.MaxPooling2D(pool_size=(2, 2)),
    layers.Dropout(0.25),

    layers.Flatten(),
    layers.Dense(1024, activation="relu"),
    layers.BatchNormalization(),
    layers.Dropout(0.5),
    layers.Dense(2, activation="softmax")
    ]
)

white_model.summary()

black_model = keras.models.clone_model(white_model)

white_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=[tf.keras.metrics.BinaryAccuracy()])
epochs = 8

history = white_model.fit(
    train_white_fields,
    epochs=epochs,
    validation_data=test_white_fields
    )

white_model.save_weights(f'archive/white_{MODEL_NAME_TIME}_rgb.h5')


black_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=[tf.keras.metrics.BinaryAccuracy()])
epochs = 8

history = black_model.fit(
    train_black_fields,
    epochs=epochs,
    validation_data=test_black_fields
    )
black_model.save_weights(f'archive/black_{MODEL_NAME_TIME}_rgb.h5')

converter_white_model = tf.lite.TFLiteConverter.from_keras_model(white_model)
converter_white_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_white_model.convert()

f = open(f'archive/white_{MODEL_NAME_TIME}_rgb.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

f = open(f'white_{MODEL_NAME}_rgb.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

converter_black_model = tf.lite.TFLiteConverter.from_keras_model(black_model)
converter_black_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_black_model.convert()

f = open(f'archive/black_{MODEL_NAME_TIME}_rgb.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

f = open(f'black_{MODEL_NAME}_rgb.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

# This code imports the necessary libraries for building a convolutional neural network (CNN) using TensorFlow and Keras.
# It also sets the image size, batch size, path to data, and model name.
# Two ImageDataGenerators are created for white fields and black fields with rotation range of 5 and fill mode set to 'nearest'.
# The flow_from_directory method is used to create train and test datasets from the respective directories.
# A CNN model is then created with 3 Conv2D layers, a Flatten layer, a Dropout layer, 2 Dense layers and an output layer with 12 classes (every possible piece).
# The model is compiled using Adam optimizer and categorical crossentropy loss function. The model is then trained for 10 epochs on both the white fields and black fields datasets.
# Finally, two TFLite models are created for both the white fields and black fields datasets using the TFLiteConverter from Keras model method.