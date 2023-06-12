import tensorflow as tf
import argparse
import sys
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras import models
from keras.preprocessing.image import ImageDataGenerator

from tensorflow.keras.applications import VGG16, VGG19, MobileNet, MobileNetV2
from functions import make_plot, make_plot_model
from timeit import default_timer as timer
import matplotlib.pyplot as plt
import numpy as np
import copy

def choose_conv_base():
    parser = argparse.ArgumentParser(description='Get network type')
    parser.add_argument('network_type', type=str, nargs='?', 
                        choices=['vgg16', 'vgg19', 'mobilenet', 'mobilenetv2'], 
                        default='vgg16', help='Type of the network (vgg16, vgg19, mobilenet, mobilenetv2)')
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

image_size_piece = (32, 32)
image_size_board = (200, 200)
batch_size = 32
EPOCHS = 30
datagen_pieces = ImageDataGenerator(
        rescale=1./255,
        horizontal_flip=False,
        fill_mode='nearest',
        validation_split=0.20
        )
datagen_boards = ImageDataGenerator(
        rescale=1./255,
        horizontal_flip=False,
        fill_mode='nearest',
        validation_split=0.20
        )

PATH_TO_DATA_PIECES = "data_one_net\\pieces"
train_generator_pieces = datagen_pieces.flow_from_directory(
    PATH_TO_DATA_PIECES,
    target_size=image_size_piece,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=True,
    batch_size=batch_size,
    subset='training'
)

# print("to jest: ",train_generator_pieces.index_array)
validation_generator_pieces = datagen_pieces.flow_from_directory(
    PATH_TO_DATA_PIECES,
    target_size=image_size_piece,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=True,
    batch_size=batch_size,
    subset='validation'
)
print("to jest: ",train_generator_pieces.index_array)
print("to jest2: ",validation_generator_pieces.index_array)
PATH_TO_DATA_BOARDS = "data_one_net\\boards"

train_generator_boards = datagen_boards.flow_from_directory(
    PATH_TO_DATA_BOARDS,
    target_size=image_size_board,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=False,
    batch_size=batch_size,
    subset='training'
)
validation_generator_boards = datagen_boards.flow_from_directory(
    PATH_TO_DATA_BOARDS,
    target_size=image_size_board,
    class_mode='categorical',
    color_mode='rgb',
    seed=1,
    shuffle=False,
    batch_size=batch_size,
    subset='validation'
)

def generator_two_img(gen1, gen2):
    genX1 = gen1
    genX2 = gen2
    while True:
        genX2.index_array = genX1.index_array
        X1i = genX1.next()
        X2i = genX2.next()
        # genX1.index_array = genX2.index_array
        yield [X1i[0], X2i[0]], X1i[1]

train_generator = generator_two_img(train_generator_pieces, train_generator_boards)
validation_generator = generator_two_img(validation_generator_pieces, validation_generator_boards)

print("to jest: ",train_generator_pieces.index_array)
print("to jest2: ",validation_generator_pieces.index_array)

input_piece = tf.keras.Input(shape=(32, 32, 3), name="piece_image")

conv_piece = conv_base(input_piece)
x_piece = layers.Flatten()(conv_piece) 
x_piece = layers.Dense(1024, activation='relu')(x_piece)

input_board = tf.keras.Input(shape=(200, 200, 3), name="board_image")
conv_board = VGG19(weights='imagenet',
                  include_top=False,
                  input_shape=(200, 200, 3))(input_board)
x_board = layers.Flatten()(conv_board) 
x_board = layers.Dense(1024, activation='relu')(x_board)


concatenated = layers.concatenate([x_piece, x_board])
x = layers.Dense(1024, activation='relu')(concatenated)
x = layers.Dense(512, activation='relu')(x)
output = layers.Dense(13, activation='softmax')(x)

model = models.Model([input_piece, input_board], output)

print(model.summary())

make_plot_model(model, f'one_net_three_inputs_{name_conv_base}.png')

print('Liczba wag poddawanych trenowaniu '
      'przed zamrożeniem bazy:', len(model.trainable_weights))

model.layers[2].trainable = False # vgg16
model.layers[3].trainable = False # vgg19

print('Liczba wag poddawanych trenowaniu '
      'po zamrożeniu bazy:', len(model.trainable_weights))

print(model.summary())

print("to jest: ",train_generator_pieces.index_array)
print("to jest2: ",validation_generator_pieces.index_array)

plt.figure(figsize=(4,80))
for i in range(1, 40, 2):
    for X_batch, Y_batch in train_generator:
        print(i)
        plt.subplot(20, 2, i)
        image = X_batch[0][0]
        plt.imshow(image)

        plt.subplot(20, 2, i+1)
        image2 = X_batch[1][0]
        plt.imshow(image2)

        break
plt.show()
print("to jest: ",train_generator_pieces.index_array)
print("to jest2: ",validation_generator_pieces.index_array)
print("to jest2: ",validation_generator_pieces.index_array.__class__)
exit()



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
      steps_per_epoch=train_generator_pieces.samples // batch_size,
      epochs=EPOCHS,
      validation_data=validation_generator,
      validation_steps=validation_generator_pieces.samples // batch_size,
      verbose=1,
      callbacks=[model_checkpoint_callback])

make_plot(history, f'one_net_two_inputs_epochs_{EPOCHS}_{name_conv_base}.png', False)
model.load_weights(checkpoint_filepath)
model.save(f'models/one_net_two_inputs_{name_conv_base}.h5')


converter_model = tf.lite.TFLiteConverter.from_keras_model(model)
converter_model.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized_model = converter_model.convert()
f = open(f'models/one_net_two_inputs_{name_conv_base}_vgg_19.tflite', "wb")
f.write(tflite_quantized_model)
f.close()

end = timer()
print("Time spend: ", end - start, "s")