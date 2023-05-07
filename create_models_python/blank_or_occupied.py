from datetime import datetime
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from keras.preprocessing.image import ImageDataGenerator
import matplotlib.pyplot as plt
from functions import make_plot
from timeit import default_timer as timer

start = timer()

image_size = (32, 32)
batch_size = 64

current_time = datetime.now().strftime("%Y-%m-%d_%H_%M_%S")

PATH_TO_DATA = 'blank_or_occupied_data'
MODEL_NAME = f'blank_or_occupied_model'
MODEL_NAME_TIME = MODEL_NAME + "_" + current_time

datagen_white_fields = ImageDataGenerator(
        rescale=1./255,
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest',
        brightness_range=[0.7,1.3],
        validation_split=0.20)
print(datagen_white_fields)
datagen_black_fields = ImageDataGenerator(
        rescale=1./255,
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest',
        brightness_range=[0.7,1.3],
        validation_split=0.20)
print(datagen_black_fields)
train_white_fields_generator = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True,
    batch_size=batch_size,
    subset='training'
)

validation_white_fields_generator = datagen_white_fields.flow_from_directory(
    f'{PATH_TO_DATA}/white_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True,
    batch_size=batch_size,
    subset='validation') # set as validation data

train_black_fields_generator = datagen_black_fields.flow_from_directory(
    f'{PATH_TO_DATA}/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True,
    batch_size=batch_size,
    subset='training'
)

validation_black_fields_generator = datagen_black_fields.flow_from_directory(
    f'{PATH_TO_DATA}/black_fields',
    target_size = image_size,
    class_mode = 'categorical',
    color_mode = 'grayscale',
    seed = 1,
    shuffle=True,
    batch_size=batch_size,
    subset='validation') # set as validation data


# Used to generate graphs, uncomment below lines to generate graphs without learn new models
# plt.figure(figsize=(12, 12))
# for i in range(0, 15):
#     plt.subplot(5, 3, i+1)
#     for X_batch, Y_batch in black_fields:
#         image = X_batch[0]
#         state = "Puste" if Y_batch[0][0] == 1 else "Zajęte"
#         plt.imshow(image, cmap='gray')
#         plt.xlim(0,32)
#         plt.ylim(32,0)
#         plt.title(state, size=14)
#         break
# plt.tight_layout()
# plt.show()
# exit()

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
        layers.Dropout(0.5),
        layers.Dense(256, activation="relu"),
        layers.Dense(2, activation="softmax"), # 2 classes
    ]
)

white_model.summary()

black_model = keras.models.clone_model(white_model)

white_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=[tf.keras.metrics.BinaryAccuracy()])
epochs = 40

history = white_model.fit(
    train_white_fields_generator,
    steps_per_epoch = train_white_fields_generator.samples // batch_size,
    validation_data = validation_white_fields_generator, 
    validation_steps = validation_white_fields_generator.samples // batch_size,
    epochs = epochs)

white_model.save(f'models/white_{MODEL_NAME}_all.h5')
make_plot(history, f'white_{MODEL_NAME}_{epochs}_epochs.png')



black_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=[tf.keras.metrics.BinaryAccuracy()])
epochs = 40

history = black_model.fit(
    train_black_fields_generator,
    steps_per_epoch = train_black_fields_generator.samples // batch_size,
    validation_data = validation_black_fields_generator, 
    validation_steps = validation_black_fields_generator.samples // batch_size,
    epochs = epochs)

black_model.save(f'models/black_{MODEL_NAME}_all.h5')
make_plot(history, f'black_{MODEL_NAME}_{epochs}_epochs.png')



converter_white_model = tf.lite.TFLiteConverter.from_keras_model(white_model)
converter_white_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_white_model.convert()

f = open(f'models\\white_{MODEL_NAME}_all.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

converter_black_model = tf.lite.TFLiteConverter.from_keras_model(black_model)
converter_black_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_black_model.convert()

f = open(f'models\\black_{MODEL_NAME}_all.tflite', "wb")
f.write(tflite_quantized_model)
f.close()
end = timer()
print(MODEL_NAME ," learn ends", end - start, "s")