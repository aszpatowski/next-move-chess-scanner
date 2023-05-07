import matplotlib.pyplot as plt
import numpy as np
import keras

# Wczytanie modelu z pliku
model = keras.models.load_model('E:\\next-move-chess-scanner\\python_notebooks_n_models\\generate_data\\black_blank_or_occupied_model_all.h5')
print(model)
# Pobranie historii uczenia
history = model.history

# Wyciągnięcie metryk accuracy i loss dla danych treningowych i testowych
print(history)
train_acc = history['accuracy']
train_loss = history['loss']
val_acc = history['val_accuracy']
val_loss = history['val_loss']

# Przygotowanie danych do wykresów
epochs = range(1, len(train_acc) + 1)

# Tworzenie wykresu z dwoma podwykresami
fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 8))

# Wykres accuracy
ax1.plot(epochs, train_acc, 'bo', label='Dokładność trenowania')
ax1.plot(epochs, val_acc, 'b', label='Dokładność walidacji')
ax1.set_title('Dokładność modelu')
ax1.set_ylabel('Dokładność')
ax1.set_ylim(0, 1)
ax1.legend()

# Wykres loss
ax2.plot(epochs, train_loss, 'bo', label='Strata trenowania')
ax2.plot(epochs, val_loss, 'b', label='Strata walidacji')
ax2.set_title('Strata modelu')
ax2.set_xlabel('Epoka')
ax2.set_ylabel('Strata')
ax2.set_ylim(0, max(train_loss))
ax2.legend()

# Ustawienie tekstu na polski
plt.rcParams.update({'font.size': 14})

# Zapisanie wykresu do pliku
plt.savefig('wykres.png', dpi=300)