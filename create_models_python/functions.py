import os
import numpy as np
import matplotlib.pyplot as plt
from keras.utils.vis_utils import plot_model

pieces = ["king", "queen", "knight", "bishop", "rook", "pawn"]
colors = ["black", "white"]
pieces_names = {"k": "king_black",
                "q": "queen_black",
                "n": "knight_black",
                "b": "bishop_black",
                "r": "rook_black",
                "p": "pawn_black",

                "K": "king_white",
                "Q": "queen_white",
                "N": "knight_white",
                "B": "bishop_white",
                "R": "rook_white",
                "P": "pawn_white",

                "blank": "blank"
                }


def get_names(path: str) -> list:
    """
    Function takes in a path as an argument and returns a list of files in that path.
    """
    return [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]


def get_fen_lists(file_name: str) -> list:
    """
    Function takes in a file name as an argument and returns a list of FEN lists. 
    It first converts the file name into a list, converts any numbers to integers, 
    replaces any blanks with 'blank', removes any dashes, creates a new list using the dictionary values, 
    reshapes it into 8x8 array and then returns it as a list.
    """

    fen = list(file_name[:-5])
    for i in range(len(fen)):
        try:
            fen[i] = int(fen[i])
        except:
            continue
    i = 0
    while (i < len(fen)):
        if type(fen[i]) is str:
            i += 1
        else:
            times = fen[i]
            fen = fen[:i] + ['blank' for x in range(times)] + fen[i + 1:]
            i += times
    fen = [value for value in fen if value != "-"]
    fen = [pieces_names[key] for key in fen]
    fen = np.array(fen).reshape(8, 8).tolist()
    return fen

def make_plot(model, name, binary=True):
    history = model.history

    if binary:
        train_acc = history['binary_accuracy']
        val_acc = history['val_binary_accuracy']
    else:
        train_acc = history['accuracy']
        val_acc = history['val_accuracy']

    train_loss = history['loss']
    val_loss = history['val_loss']

    num_epochs = len(train_acc)
    # Przygotowanie danych do wykresów
    epochs = range(1, num_epochs + 1)
    

    # Tworzenie wykresu z dwoma podwykresami
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(14, 8))

    fig.subplots_adjust(hspace=0.5)
    # Wykres accuracy
    ax1.plot(epochs, train_acc, '-', label='Dokładność trenowania',)
    ax1.plot(epochs, val_acc, '--', label='Dokładność walidacji')
    # ax1.set_xticks(range(0, num_epochs+1, 2))
    # ax1.set_xticklabels(range(0, num_epochs+1, 2))
    ax1.set_title('Dokładność modelu')
    ax1.set_xlabel('Epoka')
    ax1.set_ylabel('Dokładność')
    ax1.legend()

    # Wykres loss
    ax2.plot(epochs, train_loss, '-', label='Strata trenowania')
    ax2.plot(epochs, val_loss, '--', label='Strata walidacji')
    # ax2.set_xticks(range(0, num_epochs+1, 2))
    # ax2.set_xticklabels(range(0, num_epochs+1, 2))
    ax2.set_title('Strata modelu')
    ax2.set_xlabel('Epoka')
    ax2.set_ylabel('Strata')
    ax2.legend()

    # Ustawienie tekstu na polski
    plt.rcParams.update({'font.size': 14})

    # Zapisanie wykresu do pliku
    plt.savefig(f"graphs/{name}", dpi=300, bbox_inches='tight')
def make_plot_model(model, file_name):
    plot_model(model, to_file=f'models_scheme/{file_name}', show_shapes=True, show_layer_names=True)