import argparse
import chess.pgn
import cv2
import os
import random
import numpy as np

PIECE_TRANSLATION_TO_FEN = {
    'p_w': 'P',
    'n_w': 'N',
    'b_w': 'B',
    'r_w': 'R',
    'q_w': 'Q',
    'k_w': 'K',
    'p_b': 'p',
    'n_b': 'n',
    'b_b': 'b',
    'r_b': 'r',
    'q_b': 'q',
    'k_b': 'k'
    }

def open_pgn(file_name: str, number_games: int) -> list[str]:
    fen_boards = []
    added_games = 0
    with open(file_name) as pgn:
        game = chess.pgn.read_game(pgn)
        while game is not None and added_games<number_games:
            number_of_moves = len(list(game.mainline_moves()))
            if number_of_moves<20:
                game = chess.pgn.read_game(pgn)
                continue
            board = game.board()
            random_push = random.randint(16, number_of_moves-1)
            counter = 0
            for move in game.mainline_moves():
                board.push(move)
                counter+=1
                if counter==random_push:
                    print(counter, number_of_moves-1)
                    break
            fen_boards.append(board.fen().split(' ')[0])
            added_games+=1
            game = chess.pgn.read_game(pgn)
    return fen_boards
def load_boards(directory_name: str) -> list:
    file_list = []

    for file_name in os.listdir(directory_name):
        if file_name.endswith(".png"):
            file_path = os.path.join(directory_name, file_name)
            image = cv2.imread(file_path)
            file_list.append(image)

    return file_list
def load_pieces(directory_name: str) -> list:
    file_list = []

    for folder_num in os.listdir(directory_name):
        folder_name = os.path.join(directory_name, folder_num)
        piece_dict = {}

        for file_name in os.listdir(folder_name):
            if file_name.endswith(".png"):
                piece_name = file_name.split(".")[0]
                file_path = os.path.join(folder_name, file_name)
                image = cv2.imread(file_path, cv2.IMREAD_UNCHANGED)
                piece_dict[PIECE_TRANSLATION_TO_FEN[piece_name]] = image

        file_list.append(piece_dict)

    return file_list

def create_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)
    elif os.listdir(path):
        raise OSError(f"Directory {path} exists and is not empty")
    else:
        print(f"Directory {path} exists. OK.")

def create_chessboards(fen_boards, chessboards, sets_of_pieces, data_output):
    print('Start creating chessboards')
    create_directory(data_output)
    for fen_board in fen_boards:
        chessboard = random.choice(chessboards)
        set_of_pieces = random.choice(sets_of_pieces)
        ranks = fen_board.split('/')

        board_img = cv2.cvtColor(chessboard, cv2.COLOR_RGB2RGBA)

        y = 0
        for rank in ranks:
            x = 0
            for char in rank:
                if char.isdigit():
                    x += int(char)*50
                else:
                    piece_name = char
                    
                    piece_img = set_of_pieces[piece_name]
                    
                    mask = piece_img[:,:,3]
                    mask = mask / 255.0
                    
                    for c in range(0, 3):
                        board_img[y:y+50, x:x+50, c] = (mask * piece_img[:, :, c] + (1.0 - mask) * board_img[y:y+50, x:x+50, c])

                    x += 50
            y += 50
        
        cv2.imwrite(os.path.join("data_output", f"{fen_board.replace('/', '-')}.jpeg"), board_img)
    print('End creating chessboards')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Print the final FEN notation of the first game in a PGN file.')
    parser.add_argument('--pgn_file', type=str, help='path to the PGN file')
    parser.add_argument('--number_of_boards', type=int, help='amount of chessboards to make based on pgn file')
    parser.add_argument('--data_output', type=str, help='path where generated chessboard should be saved')
    args = parser.parse_args()
    create_chessboards(
        open_pgn(args.pgn_file, args.number_of_boards),
        load_boards('boards'),
        load_pieces('pieces'),
        args.data_output
    )

        



