import numpy as np

sample = "1b1b1b2-3r4-1rK4b-R7-R2R1k2-2Bp4-2P5-2r5.jpeg"

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

fen = list(sample[:-5])
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
for row in fen:
    print(row)

    """
    It returns:
    ['blank', 'b', 'blank', 'B', 'blank', 'b', 'blank', 'blank']
    ['blank', 'blank', 'p', 'K', 'blank', 'blank', 'q', 'blank']
    ['blank', 'blank', 'blank', 'blank', 'p', 'blank', 'r', 'B']
    ['blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'k']
    ['blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank']
    ['blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank']
    ['blank', 'blank', 'blank', 'B', 'blank', 'blank', 'blank', 'blank']
    ['blank', 'blank', 'blank', 'r', 'b', 'blank', 'blank', 'blank']
    
    """
from itertools import cycle

colors = ["black", "white"]
iter_color = cycle(colors)  # every next(color) gives white, black, white, black, white...
for row in range(0, 8):
    next(iter_color)
    for column in range(0, 8):
        color = next(iter_color)
        print(color, end=" ")
    print("\n")
