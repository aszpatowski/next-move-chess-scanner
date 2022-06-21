import os
import numpy as np

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
    return [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]


def get_fen_lists(file_name: str) -> list:


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
