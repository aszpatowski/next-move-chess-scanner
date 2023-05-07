import tensorflow as tf
import numpy as np
from PIL import Image

# Wymiary obrazka
IMG_WIDTH = 32
IMG_HEIGHT = 32
IMG_CHANNELS = 3

# Funkcja do przetwarzania obrazka na format akceptowany przez model
def process_image(image):
    # Zmiana rozmiaru obrazka na wymagany przez model
    image = image.resize((IMG_WIDTH, IMG_HEIGHT))
    
    # Konwersja obrazka do tablicy NumPy
    image_array = np.array(image)
    
    # Normalizacja wartości pikseli do przedziału [0, 1]
    image_array = image_array.astype('float32') / 255.0
    
    # Dopasowanie kanałów RGB do wymagań modelu
    #image_array = np.transpose(image_array, (2, 0, 1))
    
    # Konwersja tablicy NumPy do formatu akceptowanego przez model
    input_data = np.expand_dims(image_array, axis=0)
    
    return input_data

# Wczytanie obrazka z pliku
def load_image(file_path):
    image = Image.open(file_path)
    return image

# Funkcja do klasyfikacji koloru figury szachowej
def classify_chess_piece_color(image_path, model_path):
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
    
    print(output_data)
    # Zwrócenie wyniku predykcji ('BIAŁA' lub 'CZARNA')
    if output_data[0][0] > output_data[0][1]:
        return 'CZARNA'
    else:
        return 'BIAŁA'

# Przykład użycia
image_path = 'E:\\next-move-chess-scanner\\python_notebooks_n_models\\generate_data\\black_or_white_data\\test\\black_fields\\white\\0_6_3.jpg'
model_path = 'E:\\next-move-chess-scanner\\python_notebooks_n_models\\generate_data\\black_black_or_white_model_rgb.tflite'
chess_piece_color = classify_chess_piece_color(image_path, model_path)
print(f'Kolor figury szachowej na obrazku "{image_path}" to: {chess_piece_color}')
                