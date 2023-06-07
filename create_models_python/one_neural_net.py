import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras import models
from keras.preprocessing.image import ImageDataGenerator

from tensorflow.keras.applications import VGG16
from functions import make_plot
conv_base = VGG16(weights='imagenet',
                  include_top=False,
                  input_shape=(32, 32, 3))
print(conv_base.summary())

image_size = (32, 32)
batch_size = 64
EPOCHS = 30

datagen = ImageDataGenerator(
        rescale=1./255,
        rotation_range=5,
        horizontal_flip=False,
        fill_mode='nearest',
        brightness_range=[0.7,1.3],
        width_shift_range=0.15,
        height_shift_range=0.15,
        validation_split=0.20
        )

PATH_TO_DATA = "data_one_net\\pieces"
train_generator = datagen.flow_from_directory(
    PATH_TO_DATA,
    target_size=image_size,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=True,
    batch_size=batch_size,
    subset='training'
)
validation_generator = datagen.flow_from_directory(
    PATH_TO_DATA,
    target_size=image_size,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=True,
    batch_size=batch_size,
    subset='validation'
)

model = models.Sequential()
model.add(conv_base)
model.add(layers.Flatten())
model.add(layers.Dense(1024, activation='relu'))
model.add(layers.Dense(13, activation='softmax'))
print(model.summary())

print('Liczba wag poddawanych trenowaniu '
      'przed zamrożeniem bazy:', len(model.trainable_weights))

conv_base.trainable = False

print('Liczba wag poddawanych trenowaniu '
      'po zamrożeniu bazy:', len(model.trainable_weights))

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

history = model.fit(
      train_generator,
      steps_per_epoch=train_generator.samples // batch_size,
      epochs=EPOCHS,
      validation_data=validation_generator,
      validation_steps=validation_generator.samples // batch_size,
      verbose=2)
model.save('models/one_net_one_input.h5')

make_plot(history, f'one_net_epochs_{EPOCHS}.png', False)

converter_model = tf.lite.TFLiteConverter.from_keras_model(model)
converter_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_model.convert()
f = open(f'models/one_net_one_input.h5', "wb")
f.write(tflite_quantized_model)
f.close()