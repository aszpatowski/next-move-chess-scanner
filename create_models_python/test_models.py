import tensorflow as tf
import numpy as np
from PIL import Image
import os
import re

# Wymiary obrazka
IMG_WIDTH = 32
IMG_HEIGHT = 32
IMG_CHANNELS = 3

import os
import re

def pieces(folder_path):
    files_dict = {}
    pattern = re.compile(r"(.+)_([a-z-]+)\.png")  # szukanie nazwy bierki i koloru

    for file_name in os.listdir(folder_path):
        if file_name.endswith(".png"):  # szukanie plików png
            file_path = os.path.join(folder_path, file_name)
            color, piece = "blank", "blank"  # wartości domyślne, jeśli nie uda się wyodrębnić koloru i nazwy bierki
            match = pattern.match(file_name)
            if match:
                color, piece = match.group(1, 2)

            files_dict[file_path] = [color, piece]

    return files_dict

# Funkcja do przetwarzania obrazka na format akceptowany przez model
def process_image(image, grey = False):
    
    if grey:
        image = image.convert('L')
    # Zmiana rozmiaru obrazka na wymagany przez model

    image = image.resize((IMG_WIDTH, IMG_HEIGHT))
    
    # Konwersja obrazka do tablicy NumPy
    image_array = np.array(image)
    
    # Normalizacja wartości pikseli do przedziału [0, 1]
    image_array = image_array.astype('float32') / 255.0
    
    # Konwersja tablicy NumPy do formatu akceptowanego przez model
    input_data = np.expand_dims(image_array, axis=0)
    
    return input_data

# Wczytanie obrazka z pliku
def load_image(file_path):
    image = Image.open(file_path)
    return image

# Funkcja do klasyfikacji koloru figury szachowej
def classify_blank_or_occupied(image_path, model_path, correct_prediction):
    # Wczytanie obrazka z pliku
    image = load_image(image_path)
    
    # Przetworzenie obrazka do formatu akceptowanego przez model
    input_data = process_image(image)
    
    # Wczytanie modelu TensorFlow Lite
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    # Przekazanie danych wejściowych do modelu i wykonanie predykcji
    input_details = interpreter.get_input_details()
    #print(input_details)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_details = interpreter.get_output_details()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    
    if output_data[0][0] > output_data[0][1]:
        return 'EMPTY'
    else:
        return 'OCCUPIED'
def classify_chess_piece_color(image_path, model_path, correct_prediction):
    # Wczytanie obrazka z pliku
    image = load_image(image_path)
    
    # Przetworzenie obrazka do formatu akceptowanego przez model
    input_data = process_image(image)
    
    # Wczytanie modelu TensorFlow Lite
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    # Przekazanie danych wejściowych do modelu i wykonanie predykcji
    input_details = interpreter.get_input_details()
    #print(input_details)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_details = interpreter.get_output_details()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    
    # Zwrócenie wyniku predykcji ('BIAŁA' lub 'CZARNA')
    if output_data[0][0] > output_data[0][1]:
        return 'BLACK'
    else:
        return 'WHITE'
def classify_chess_piece(image_path, model_path, correct_prediction):
    # Wczytanie obrazka z pliku
    image = load_image(image_path)
    
    # Przetworzenie obrazka do formatu akceptowanego przez model
    input_data = process_image(image)
    
    # Wczytanie modelu TensorFlow Lite
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    # Przekazanie danych wejściowych do modelu i wykonanie predykcji
    input_details = interpreter.get_input_details()
    #print(input_details)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_details = interpreter.get_output_details()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    
    # Zwrócenie wyniku predykcji ('BIAŁA' lub 'CZARNA')
    figures = ["Bishop", "King", "Knight", "Pawn", "Queen" ,"Rook"]
    if output_data[0][0] > output_data[0][1]:
        return 'CZARNA'
    else:
        return 'BIAŁA'
if __name__=='__main__':
    # Przykład użycia
    # image_path = 'E:\\next-move-chess-scanner\\python_notebooks_n_models\\generate_data\\black_or_white_data\\test\\black_fields\\white\\0_6_3.jpg'
    # model_path = 'E:\\next-move-chess-scanner\\python_notebooks_n_models\\generate_data\\black_black_or_white_model_rgb.tflite'
    # chess_piece_color = classify_chess_piece_color(image_path, model_path)
    # print(f'Kolor figury szachowej na obrazku "{image_path}" to: {chess_piece_color}')
    for x,y in pieces('test_data').items():
        print(x,y)

                