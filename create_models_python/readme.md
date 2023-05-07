## Steps
1. Run positions generator. (Make sure you have installed correct modules, check requirements.txt)
    ```
    python generator/generator.py --pgn_file C:\chess\next-move-chess-scanner\python_notebooks_n_models\generate_data\generator\ficsgamesdb_202301_chess_nomovetimes_284372 --number_of_boards 6000 --data_output raw_data
    ```
2. Run create_separate_fields.py
    ```
    python create_separate_fields.py --start 0 --end 6000
    ```
3. Run create_datasets.py to create seperate datasets for every neural network models
    ```
    python create_datasets.py --dataset_path data
    ```
4. Run one by one, to train the network and create tflite models
    - Run blank_or_occupied.py
    ```
    python blank_or_occupied.py
    ```
    - Run black_or_white.py
    ```
    python black_or_white.py
    ```
    - Run what_piece.py
    ```
    python what_piece.py
    ```