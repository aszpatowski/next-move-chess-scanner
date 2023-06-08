import tensorflow as tf
import argparse
import tempfile
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras import models
from keras.preprocessing.image import ImageDataGenerator

from tensorflow.keras.applications import VGG16, VGG19, MobileNet, MobileNetV2
from functions import make_plot
from timeit import default_timer as timer

def choose_conv_base():
    parser = argparse.ArgumentParser(description='Get network type')
    parser.add_argument('network_type', type=str, nargs='?', choices=['vgg16', 'vgg19', 'mobilenet', 'mobilenetv2'], default='vgg16', help='Type of the network (vgg16, vgg19, mobilenet, mobilenetv2)')
    args = parser.parse_args()
    network_type = args.network_type
    if network_type == 'vgg16':
        conv_base = VGG16(weights='imagenet',
                  include_top=False,
                  input_shape=(32, 32, 3))
    elif network_type == 'vgg19':
        conv_base = VGG19(weights='imagenet',
                  include_top=False,
                  input_shape=(32, 32, 3))
    elif network_type == 'mobilenet':
        conv_base = MobileNet(weights='imagenet',
                  include_top=False,
                  input_shape=(32, 32, 3))
    elif network_type == 'mobilenetv2':
        conv_base = MobileNetV2(weights='imagenet',
                  include_top=False,
                  input_shape=(32, 32, 3))
    return (conv_base, network_type)

start = timer()

conv_base, name_conv_base = choose_conv_base()
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

print(model.summary())

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

checkpoint_filepath = './tmp/checkpoint'

model_checkpoint_callback = tf.keras.callbacks.ModelCheckpoint(
    filepath=checkpoint_filepath,
    save_weights_only=True,
    monitor='val_accuracy',
    mode='max',
    save_best_only=True)


history = model.fit(
      train_generator,
      steps_per_epoch=train_generator.samples // batch_size,
      epochs=EPOCHS,
      validation_data=validation_generator,
      validation_steps=validation_generator.samples // batch_size,
      verbose=1,
      callbacks=[model_checkpoint_callback])

make_plot(history, f'one_net_epochs_{EPOCHS}_{name_conv_base}.png', False)
model.load_weights(checkpoint_filepath)
model.save(f'models/one_net_one_input_{name_conv_base}.h5')


converter_model = tf.lite.TFLiteConverter.from_keras_model(model)
converter_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_model.convert()
f = open(f'models/one_net_one_input_{name_conv_base}.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

end = timer()
print("Time spend: ", end - start, "s")