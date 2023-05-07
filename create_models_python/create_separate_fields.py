import sys
import os
import cv2
import random
import numpy as np
import argparse
from itertools import cycle
from functions import get_names, get_fen_lists



if __name__ == "__main__":

    # create a list of pieces and colors 
    pieces = ["king", "queen", "knight", "bishop", "rook", "pawn"]
    colors = ["black", "white"]

    # create variables for the paths to data
    data_black = "data\\black_fields"
    data_white = "data\\white_fields"

    # create variables for the paths to raw data 
    path = "raw_data/"  # https://www.kaggle.com/datasets/koryakinp/chess-positions

    # get the names of all files in the directories 
    file_names = get_names(path)

    parser = argparse.ArgumentParser()
    parser.add_argument('--start', type=int, required=True)
    parser.add_argument('--end', type=int ,  required=True)

    args = parser.parse_args()

    if args.start <= args.end:
        start = args.start
        end = args.end
    else:
        print("Bad arguments")
        sys.exit(0)

    img = cv2.imread(path + file_names[0])
    height, width, channels = img.shape
    print("height=", height)
    print("width=", width)
    print("channels=", channels)
    one_field_size = height // 8
    print("one_field=", one_field_size)
    create_directory_command = f"mkdir {data_black}\\blank & mkdir  {data_white}\\blank "

    for color in colors:
        for piece in pieces:
            create_directory_command += f"& mkdir {data_black}\\{piece}_{color} & mkdir {data_white}\\{piece}_{color} "
    print(create_directory_command)
    os.system(create_directory_command)

    black_background = np.zeros((one_field_size, one_field_size, 3), np.uint8)
    white_background = np.zeros((one_field_size, one_field_size, 3), np.uint8)
    white_background[:, :] = (255, 255, 255)
    backgrounds = (black_background, white_background)
    iter_color = cycle(colors)  # every next(color) gives white, black, white, black, white...
    for sample_number in range(start, end):
        photo = cv2.imread(path + file_names[sample_number])
        fen = get_fen_lists(file_names[sample_number])
        blank_fields = 0
        for row in range(0, 8):
            next(iter_color)
            for column in range(0, 8):
                color = next(iter_color)
                if fen[row][column] == 'blank' and (random.randrange(0, 7) < 6 or blank_fields >= 2):  # most are blank
                    continue

                random_shift_row, random_shift_column = random.randrange(-4, 5), random.randrange(-4, 5)
                random_background = random.choice(backgrounds).copy()

                start_row = row * one_field_size + random_shift_row
                end_row = (row+1) * one_field_size + random_shift_row
                start_column = column * one_field_size + random_shift_column
                end_column = (column+1) * one_field_size + random_shift_column

                background_start_row = 0
                background_end_row = one_field_size

                background_start_column = 0
                background_end_column = one_field_size
                if start_row < 0:
                    background_start_row += -start_row
                    start_row = 0

                if end_row > height:
                    background_end_row += height-end_row
                    end_row = height
                if start_column < 0:
                    background_start_column += -start_column
                    start_column = 0

                if end_column > width:
                    background_end_column += width-end_column
                    end_column = width
                random_background[background_start_row:background_end_row,
                background_start_column:background_end_column, :] = photo[start_row:end_row, start_column:end_column, :]
                path_to_save = f"data\\{color}_fields\\{fen[row][column]}\\{sample_number}_{row}_{column}.jpg"
                cv2.imwrite(path_to_save, random_background)
                print(f"Saved in: {path_to_save}")
                if fen[row][column]=='blank':
                    blank_fields += 1
    
