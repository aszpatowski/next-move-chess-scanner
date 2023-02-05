import sys
import os
import cv2
import random
import numpy as np
from itertools import cycle
from functions import get_names, get_fen_lists



if __name__ == "__main__":

    # create a list of pieces and colors 
    pieces = ["king", "queen", "knight", "bishop", "rook", "pawn"]
    colors = ["black", "white"]

    # create variables for the paths to train and test data
    data_black_train = "data\\train\\black_fields"
    data_white_train = "data\\train\\white_fields"
    data_black_test = "data\\test\\black_fields"
    data_white_test = "data\\test\\white_fields"

    # create variables for the paths to raw train and test data 
    path = "raw_data"  # https://www.kaggle.com/datasets/koryakinp/chess-positions
    path_train = path + "/train/"
    path_test = path + "/test/"

    # get the names of all files in the train and test directories 
    train_names = get_names(path_train)
    test_names = get_names(path_test)

    if len(sys.argv) < 5:
        print("Give arguments")
        sys.exit(0)
    else:
        try:
            if sys.argv[1] <= sys.argv[2] and sys.argv[3] <= sys.argv[4]:
                start_train = int(sys.argv[1])
                end_train = int(sys.argv[2])
                start_test = int(sys.argv[3])
                end_test = int(sys.argv[4])
            else:
                print("Bad arguments")
                sys.exit(0)
        except:
            print("Bad arguments")
            sys.exit(0)

    img = cv2.imread(path_train + train_names[0])
    height, width, channels = img.shape
    print("height=", height)
    print("width=", width)
    print("channels=", channels)
    one_field_size = height // 8
    print("one_field=", one_field_size)
    create_directory_command = f"mkdir {data_black_train}\\blank & mkdir  {data_white_train}\\blank "
    create_directory_command += f"& mkdir {data_black_test}\\blank & mkdir  {data_white_test}\\blank "
    # I know that i could write it better
    for color in colors:
        for piece in pieces:
            create_directory_command += f"& mkdir {data_black_train}\\{piece}_{color} & mkdir {data_white_train}\\{piece}_{color} "
    for color in colors:
        for piece in pieces:
            create_directory_command += f"& mkdir {data_black_test}\\{piece}_{color} & mkdir {data_white_test}\\{piece}_{color} "
    print(create_directory_command)
    os.system(create_directory_command)

    black_background = np.zeros((one_field_size, one_field_size, 3), np.uint8)
    white_background = np.zeros((one_field_size, one_field_size, 3), np.uint8)
    white_background[:, :] = (255, 255, 255)
    backgrounds = (black_background, white_background)
    iter_color = cycle(colors)  # every next(color) gives white, black, white, black, white...
    for sample_number in range(start_train, end_train):
        photo = cv2.imread(path_train + train_names[sample_number])
        fen = get_fen_lists(train_names[sample_number])
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
                path_to_save = f"data\\train\\{color}_fields\\{fen[row][column]}\\{sample_number}_{row}_{column}.jpg"
                cv2.imwrite(path_to_save, random_background)
                print(f"Saved in: {path_to_save}")
                if fen[row][column]=='blank':
                    blank_fields += 1
    iter_color = cycle(colors)  # every next(color) gives white, black, white, black, white...
    for sample_number in range(start_test, end_test):
        photo = cv2.imread(path_test + test_names[sample_number])
        fen = get_fen_lists(test_names[sample_number])
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
                end_row = (row + 1) * one_field_size + random_shift_row
                start_column = column * one_field_size + random_shift_column
                end_column = (column + 1) * one_field_size + random_shift_column

                background_start_row = 0
                background_end_row = one_field_size

                background_start_column = 0
                background_end_column = one_field_size
                if start_row < 0:
                    background_start_row += -start_row
                    start_row = 0

                if end_row > height:
                    background_end_row += height - end_row
                    end_row = height
                if start_column < 0:
                    background_start_column += -start_column
                    start_column = 0

                if end_column > width:
                    background_end_column += width - end_column
                    end_column = width
                random_background[background_start_row:background_end_row,
                background_start_column:background_end_column, :] = photo[start_row:end_row, start_column:end_column, :]
                path_to_save = f"data\\test\\{color}_fields\\{fen[row][column]}\\{sample_number}_{row}_{column}.jpg"
                cv2.imwrite(path_to_save, random_background)
                print(f"Saved in: {path_to_save}")
                if fen[row][column]=='blank':
                    blank_fields += 1
